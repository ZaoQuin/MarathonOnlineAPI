import pandas as pd
import json
import sys
import os
import warnings
warnings.filterwarnings('ignore')
sys.stdout.reconfigure(encoding='utf-8')

# Import các hàm từ file modal.py
from modal import (
    prepare_marathon_data, analyze_per_user, extract_features,
    detect_anomalies_isolation_forest, detect_anomalies_lof
)

def validate_record(record_json):
    """
    Kiểm tra tính hợp lệ của một bản ghi marathon trước khi lưu vào cơ sở dữ liệu

    Args:
        record_json (str): Chuỗi JSON chứa thông tin của bản ghi cần kiểm tra

    Returns:
        dict: Kết quả đánh giá bao gồm trạng thái, điểm rủi ro gian lận, loại gian lận và ghi chú
    """
    try:
        # Parse dữ liệu JSON từ input
        record = json.loads(record_json)

        # Tạo DataFrame từ bản ghi đơn lẻ
        record_df = pd.DataFrame([{
            'Id': record.get('id', ''),
            'UserId': record.get('user', {}).get('id', ''),
            'TotalSteps': record.get('steps', 0),
            'TotalDistance': record.get('distance', 0.0),
            'TimeTaken': record.get('timeTaken', 0),
            'AvgSpeed': record.get('avgSpeed', 0.0),
            'Timestamp': record.get('timestamp', '')
        }])

        # Kiểm tra các điều kiện cơ bản
        if not validate_basic_conditions(record_df):
            return {
                "approvalStatus": "REJECTED",
                "fraudRisk": 100.0,
                "fraudType": "Dữ liệu không hợp lệ",
                "reviewNote": "Dữ liệu không đạt điều kiện cơ bản: số bước, khoảng cách hoặc thời gian âm/không hợp lệ"
            }

        # Chuẩn bị dữ liệu
        processed_df = prepare_marathon_data(record_df)

        # Lấy dữ liệu lịch sử của người dùng nếu có
        user_id = processed_df['UserId'].iloc[0]
        user_history = load_user_history(user_id)

        # Gộp dữ liệu lịch sử với bản ghi mới để phân tích ngữ cảnh
        if user_history is not None and not user_history.empty:
            analysis_df = pd.concat([user_history, processed_df], ignore_index=True)
        else:
            # Không có lịch sử, chỉ phân tích bản ghi hiện tại với các ngưỡng cố định
            return validate_single_record(processed_df)

        # Phân tích theo người dùng
        analysis_df, user_stats = analyze_per_user(analysis_df)

        # Xác định bản ghi mới (index cuối cùng)
        last_index = analysis_df.index[-1]

        # Kiểm tra các điều kiện đáng ngờ dựa trên lịch sử người dùng
        result = validate_with_user_history(analysis_df, last_index, user_stats)

        # Trả về kết quả
        return result

    except Exception as e:
        print(f"Lỗi khi phân tích bản ghi: {e}")
        # Trả về kết quả lỗi
        return {
            "approvalStatus": "PENDING",
            "fraudRisk": 50.0,
            "fraudType": "Lỗi xử lý",
            "reviewNote": f"Có lỗi khi phân tích bản ghi: {str(e)}"
        }

def validate_basic_conditions(record_df):
    """Kiểm tra các điều kiện cơ bản của bản ghi"""
    # Kiểm tra dữ liệu âm hoặc không hợp lệ
    if record_df['TotalSteps'].iloc[0] < 0 or pd.isna(record_df['TotalSteps'].iloc[0]):
        return False
    if record_df['TotalDistance'].iloc[0] < 0 or pd.isna(record_df['TotalDistance'].iloc[0]):
        return False
    if record_df['TimeTaken'].iloc[0] <= 0 or pd.isna(record_df['TimeTaken'].iloc[0]):
        return False

    # Kiểm tra khoảng cách và số bước có hợp lý không
    distance = record_df['TotalDistance'].iloc[0]  # km
    steps = record_df['TotalSteps'].iloc[0]
    time_taken = record_df['TimeTaken'].iloc[0]  # minutes

    # Kiểm tra số bước quá thấp so với khoảng cách
    if steps > 0 and distance > 0:
        distance_per_step = distance * 1000 / steps  # meters per step
        if distance_per_step > 2.0:  # Trung bình bước dài khoảng 0.7-0.8m
            return False

    # Kiểm tra tốc độ quá cao
    if time_taken > 0 and distance > 0:
        speed = distance / (time_taken / 60)  # km/h
        if speed > 25.0:  # Tốc độ chạy người thường không quá 25km/h
            return False

    return True

def validate_single_record(processed_df):
    """Kiểm tra bản ghi đơn lẻ khi không có lịch sử"""
    # Lấy các thông số cơ bản
    steps = processed_df['TotalSteps'].iloc[0]
    distance = processed_df['TotalDistance'].iloc[0]  # km
    time_taken = processed_df['TimeTaken'].iloc[0]  # minutes

    # Tính toán các chỉ số phụ
    speed = distance / (time_taken / 60) if time_taken > 0 else 0  # km/h
    distance_per_step = (distance * 1000) / steps if steps > 0 else 0  # meters per step

    # Thiết lập ngưỡng cố định cho các trường hợp không có lịch sử
    fraud_risk = 0
    fraud_type = None
    review_note = "Bản ghi hợp lệ"

    # Kiểm tra tốc độ quá cao (>20km/h)
    if speed > 20:
        fraud_risk = max(fraud_risk, 90)
        fraud_type = "Sử dụng phương tiện"
        review_note = f"Tốc độ {speed:.2f}km/h quá cao cho chạy bộ."
    elif speed > 15:
        fraud_risk = max(fraud_risk, 70)
        fraud_type = "Tốc độ bất thường"
        review_note = f"Tốc độ {speed:.2f}km/h cao bất thường."

    # Kiểm tra khoảng cách trên mỗi bước
    if distance_per_step > 1.5:
        fraud_risk = max(fraud_risk, 80)
        fraud_type = "Khai báo sai số bước"
        review_note = f"Khoảng cách trên mỗi bước ({distance_per_step:.2f}m) quá cao."
    elif distance_per_step > 1.0:
        fraud_risk = max(fraud_risk, 60)
        fraud_type = "Dữ liệu đáng ngờ"
        review_note = f"Khoảng cách trên mỗi bước ({distance_per_step:.2f}m) cao bất thường."

    # Kiểm tra số bước quá lớn cho khoảng thời gian (>250 bước/phút)
    steps_per_minute = steps / time_taken if time_taken > 0 else 0
    if steps_per_minute > 250:
        fraud_risk = max(fraud_risk, 75)
        fraud_type = "Khai báo sai số bước"
        review_note = f"Tần suất bước ({steps_per_minute:.0f} bước/phút) quá cao."

    # Xác định trạng thái phê duyệt dựa trên điểm rủi ro
    approval_status = "APPROVED"
    if fraud_risk >= 70:
        approval_status = "REJECTED"
    elif fraud_risk >= 40:
        approval_status = "PENDING"

    return {
        "approvalStatus": approval_status,
        "fraudRisk": float(fraud_risk),
        "fraudType": fraud_type if fraud_type else "Hợp lệ",
        "reviewNote": review_note
    }

def validate_with_user_history(analysis_df, new_record_index, user_stats):
    """Kiểm tra bản ghi mới dựa trên lịch sử của người dùng"""
    # Trích xuất bản ghi mới
    new_record = analysis_df.iloc[new_record_index]

    # Tính toán các độ lệch so với thống kê trung bình của người dùng
    user_id = new_record['UserId']

    # Khởi tạo các biến đánh giá
    fraud_risk = 0
    fraud_type = None
    review_note = "Bản ghi hợp lệ"

    # Kiểm tra các độ lệch đáng ngờ
    if 'StepDeviation' in analysis_df.columns and abs(new_record['StepDeviation']) > 3:
        fraud_risk = max(fraud_risk, 75)
        fraud_type = "Khai báo sai số bước"
        review_note = f"Số bước đi lệch {abs(new_record['StepDeviation']):.2f} độ lệch chuẩn so với lịch sử."

    if 'SpeedDeviation' in analysis_df.columns and abs(new_record['SpeedDeviation']) > 3:
        fraud_risk = max(fraud_risk, 80)
        fraud_type = "Tốc độ bất thường"
        review_note = f"Tốc độ lệch {abs(new_record['SpeedDeviation']):.2f} độ lệch chuẩn so với lịch sử."

    if 'DistPerStepDeviation' in analysis_df.columns and abs(new_record['DistPerStepDeviation']) > 3:
        fraud_risk = max(fraud_risk, 85)
        fraud_type = "Mẫu bước đi bất thường"
        review_note = f"Khoảng cách trên mỗi bước lệch {abs(new_record['DistPerStepDeviation']):.2f} độ lệch chuẩn so với lịch sử."

    # Kiểm tra phân tích bất thường dựa trên đặc trưng
    try:
        # Trích xuất đặc trưng từ dữ liệu
        features_scaled, _ = extract_features(analysis_df)

        # Đảm bảo số lượng mẫu đủ lớn
        if len(features_scaled) >= 5:
            # Phát hiện bất thường bằng Isolation Forest
            if_predictions, _ = detect_anomalies_isolation_forest(features_scaled, contamination=0.1)
            if if_predictions[new_record_index] == 1:  # 1 = bất thường
                fraud_risk = max(fraud_risk, 70)
                if fraud_type is None:
                    fraud_type = "Dữ liệu bất thường"
                review_note += " Isolation Forest xác định là bất thường."

            # Phát hiện bất thường bằng LOF
            lof_predictions, _ = detect_anomalies_lof(features_scaled, contamination=0.1)
            if lof_predictions[new_record_index] == 1:  # 1 = bất thường
                fraud_risk = max(fraud_risk, 65)
                if fraud_type is None:
                    fraud_type = "Dữ liệu bất thường"
                review_note += " LOF xác định là điểm ngoại lai cục bộ."
    except Exception as e:
        print(f"Lỗi khi phân tích bất thường: {e}")

    # Xác định trạng thái phê duyệt dựa trên điểm rủi ro
    approval_status = "APPROVED"
    if fraud_risk >= 70:
        approval_status = "REJECTED"
    elif fraud_risk >= 40:
        approval_status = "PENDING"

    # Cập nhật nếu không có vấn đề gì
    if fraud_risk == 0:
        fraud_type = "Hợp lệ"

    return {
        "approvalStatus": approval_status,
        "fraudRisk": float(fraud_risk),
        "fraudType": fraud_type,
        "reviewNote": review_note
    }

def load_user_history(user_id):
    """
    Tải lịch sử hoạt động của người dùng từ tệp CSV hoặc cơ sở dữ liệu

    Args:
        user_id: ID của người dùng

    Returns:
        DataFrame: Lịch sử hoạt động hoặc None nếu không có
    """
    # Đường dẫn đến tệp lịch sử (nên cung cấp qua tham số hoặc config)
    history_path = os.getenv('USER_HISTORY_PATH', 'user_history')

    # Tên tệp lịch sử cho người dùng cụ thể
    user_file = os.path.join(history_path, f"user_{user_id}_history.csv")

    try:
        # Kiểm tra xem tệp có tồn tại không
        if os.path.exists(user_file):
            # Đọc lịch sử người dùng
            user_history = pd.read_csv(user_file)
            print(f"Đã đọc {len(user_history)} bản ghi lịch sử của người dùng {user_id}")
            return user_history
        else:
            print(f"Không tìm thấy lịch sử cho người dùng {user_id}")
            return None
    except Exception as e:
        print(f"Lỗi khi đọc lịch sử người dùng: {e}")
        return None

if __name__ == "__main__":
    try:
        # Kiểm tra đối số dòng lệnh
        if len(sys.argv) < 2:
            print("Cách sử dụng: python record_validator.py <chuỗi_json_bản_ghi> [--file]")
            sys.exit(1)

        # Xác định xem đối số là đường dẫn file hay chuỗi JSON
        is_file_path = False
        if len(sys.argv) > 2 and sys.argv[2] == "--file":
            is_file_path = True

        # Lấy nội dung JSON từ tệp hoặc đối số
        if is_file_path:
            file_path = sys.argv[1]
            print(f"Đọc JSON từ tệp: {file_path}")
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    record_json = f.read().strip()
            except Exception as e:
                print(f"Lỗi khi đọc tệp JSON: {e}")
                sys.exit(1)
        else:
            record_json = sys.argv[1]

        # Kiểm tra tính hợp lệ của bản ghi
        result = validate_record(record_json)

        # In kết quả dưới dạng JSON
        print("\n--- BEGIN JSON RESULT ---")
        print(json.dumps(result, ensure_ascii=False))
        print("--- END JSON RESULT ---")
    except Exception as e:
        print(f"Lỗi không mong đợi: {e}")
        # Trả về JSON báo lỗi
        print(json.dumps({
            "approvalStatus": "PENDING",
            "fraudRisk": 50.0,
            "fraudType": "Lỗi hệ thống",
            "reviewNote": f"Lỗi không mong đợi: {str(e)}"
        }, ensure_ascii=False))