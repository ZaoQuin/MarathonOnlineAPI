import pandas as pd
import json
import sys
import os
import warnings
warnings.filterwarnings('ignore')
sys.stdout.reconfigure(encoding='utf-8')

from modal import (
    prepare_marathon_data, analyze_per_user, extract_features,
    detect_anomalies_isolation_forest, detect_anomalies_lof
)

def validate_record(record_json):
    """
    Kiểm tra tính hợp lệ của một bản ghi marathon trước khi lưu vào cơ sở dữ liệu.

    Args:
        record_json (str): Chuỗi JSON chứa thông tin của bản ghi cần kiểm tra.

    Returns:
        dict: Kết quả đánh giá bao gồm trạng thái, điểm rủi ro gian lận, loại gian lận và ghi chú.
    """
    try:
        record = json.loads(record_json)
        record_df = pd.DataFrame([{
            'Id': record.get('id', ''),
            'UserId': record.get('user', {}).get('id', ''),
            'TotalSteps': record.get('steps', 0),
            'TotalDistance': record.get('distance', 0.0),
            'TimeTaken': record.get('timeTaken', 0),
            'AvgSpeed': record.get('avgSpeed', 0.0),
            'Timestamp': record.get('timestamp', ''),
            'heartRate': record.get('heartRate', None)
        }])

        if not validate_basic_conditions(record_df):
            return {
                "approvalStatus": "REJECTED",
                "fraudRisk": 100.0,
                "fraudType": "Dữ liệu không hợp lệ",
                "reviewNote": "Dữ liệu không đạt điều kiện cơ bản: số bước, khoảng cách, thời gian hoặc nhịp tim không hợp lệ"
            }

        processed_df = prepare_marathon_data(record_df)
        user_id = processed_df['UserId'].iloc[0]
        user_history = load_user_history(user_id)

        if user_history is not None and not user_history.empty:
            analysis_df = pd.concat([user_history, processed_df], ignore_index=True)
        else:
            return validate_single_record(processed_df)

        analysis_df, user_stats = analyze_per_user(analysis_df)
        last_index = analysis_df.index[-1]
        result = validate_with_user_history(analysis_df, last_index, user_stats)
        return result

    except Exception as e:
        print(f"Lỗi khi phân tích bản ghi: {e}")
        return {
            "approvalStatus": "PENDING",
            "fraudRisk": 50.0,
            "fraudType": "Lỗi xử lý",
            "reviewNote": f"Có lỗi khi phân tích bản ghi: {str(e)}"
        }

def validate_basic_conditions(record_df):
    """
    Kiểm tra các điều kiện cơ bản của bản ghi.

    Args:
        record_df (pd.DataFrame): DataFrame chứa bản ghi.

    Returns:
        bool: True nếu hợp lệ, False nếu không.
    """
    if record_df['TotalSteps'].iloc[0] < 0 or pd.isna(record_df['TotalSteps'].iloc[0]):
        return False
    if record_df['TotalDistance'].iloc[0] < 0 or pd.isna(record_df['TotalDistance'].iloc[0]):
        return False
    if record_df['TimeTaken'].iloc[0] <= 0 or pd.isna(record_df['TimeTaken'].iloc[0]):
        return False

    distance = record_df['TotalDistance'].iloc[0]
    steps = record_df['TotalSteps'].iloc[0]
    time_taken = record_df['TimeTaken'].iloc[0]

    if steps > 0 and distance > 0:
        distance_per_step = distance * 1000 / steps
        if distance_per_step > 2.0:
            return False

    if time_taken > 0 and distance > 0:
        speed = distance / (time_taken / 60)
        if speed > 25.0:
            return False

    if 'heartRate' in record_df.columns and pd.notna(record_df['heartRate'].iloc[0]):
        heart_rate = record_df['heartRate'].iloc[0]
        if heart_rate < 40 or heart_rate > 220:
            return False

    return True

def validate_single_record(processed_df):
    """
    Kiểm tra bản ghi đơn lẻ khi không có lịch sử.

    Args:
        processed_df (pd.DataFrame): DataFrame đã xử lý.

    Returns:
        dict: Kết quả đánh giá.
    """
    steps = processed_df['TotalSteps'].iloc[0]
    distance = processed_df['TotalDistance'].iloc[0]
    time_taken = processed_df['TimeTaken'].iloc[0]
    heart_rate = processed_df['heartRate'].iloc[0] if 'heartRate' in processed_df.columns and pd.notna(processed_df['heartRate'].iloc[0]) else None

    speed = distance / (time_taken / 60) if time_taken > 0 else 0
    distance_per_step = (distance * 1000) / steps if steps > 0 else 0
    fraud_risk = 0
    fraud_type = None
    review_note = "Bản ghi hợp lệ"

    if speed > 20:
        fraud_risk = max(fraud_risk, 90)
        fraud_type = "Sử dụng phương tiện"
        review_note = f"Tốc độ {speed:.2f}km/h quá cao cho chạy bộ."
    elif speed > 15:
        fraud_risk = max(fraud_risk, 70)
        fraud_type = "Tốc độ bất thường"
        review_note = f"Tốc độ {speed:.2f}km/h cao bất thường."

    if distance_per_step > 1.5:
        fraud_risk = max(fraud_risk, 80)
        fraud_type = "Khai báo sai số bước"
        review_note = f"Khoảng cách trên mỗi bước ({distance_per_step:.2f}m) quá cao."
    elif distance_per_step > 1.0:
        fraud_risk = max(fraud_risk, 60)
        fraud_type = "Dữ liệu đáng ngờ"
        review_note = f"Khoảng cách trên mỗi bước ({distance_per_step:.2f}m) cao bất thường."

    steps_per_minute = steps / time_taken if time_taken > 0 else 0
    if steps_per_minute > 250:
        fraud_risk = max(fraud_risk, 75)
        fraud_type = "Khai báo sai số bước"
        review_note = f"Tần suất bước ({steps_per_minute:.0f} bước/phút) quá cao."

    if heart_rate is not None and heart_rate > 0:
        if heart_rate < 60 and steps > 10000:
            fraud_risk = max(fraud_risk, 85)
            fraud_type = "Nhịp tim bất thường"
            review_note = f"Nhịp tim {heart_rate:.1f} bpm quá thấp so với số bước {steps}."

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
    """
    Kiểm tra bản ghi mới dựa trên lịch sử của người dùng.

    Args:
        analysis_df (pd.DataFrame): DataFrame chứa dữ liệu lịch sử và bản ghi mới.
        new_record_index (int): Chỉ số của bản ghi mới.
        user_stats (dict): Thống kê người dùng.

    Returns:
        dict: Kết quả đánh giá.
    """
    new_record = analysis_df.iloc[new_record_index]
    user_id = new_record['UserId']
    fraud_risk = 0
    fraud_type = None
    review_note = "Bản ghi hợp lệ"

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

    if ('heartRate' in analysis_df.columns and pd.notna(new_record['heartRate']) and
            'HeartRateDeviation' in analysis_df.columns and pd.notna(new_record['HeartRateDeviation'])):
        if abs(new_record['HeartRateDeviation']) > 3:
            fraud_risk = max(fraud_risk, 80)
            fraud_type = "Nhịp tim bất thường"
            review_note = f"Nhịp tim lệch {abs(new_record['HeartRateDeviation']):.2f} độ lệch chuẩn so với lịch sử."

    if user_id in user_stats and 'step_correlations' in user_stats[user_id]:
        if user_stats[user_id]['step_correlations'].get('TotalDistance', 1.0) < 0.5:
            fraud_risk = max(fraud_risk, 70)
            fraud_type = "Tương quan bất thường"
            review_note = "Tương quan thấp giữa số bước và khoảng cách so với lịch sử."

    try:
        features_scaled, _ = extract_features(analysis_df)
        if len(features_scaled) >= 5:
            if_predictions, _ = detect_anomalies_isolation_forest(features_scaled, contamination=0.1)
            if if_predictions[new_record_index] == 1:
                fraud_risk = max(fraud_risk, 70)
                if fraud_type is None:
                    fraud_type = "Dữ liệu bất thường"
                review_note += " Isolation Forest xác định là bất thường."

            lof_predictions, _ = detect_anomalies_lof(features_scaled, contamination=0.1)
            if lof_predictions[new_record_index] == 1:
                fraud_risk = max(fraud_risk, 65)
                if fraud_type is None:
                    fraud_type = "Dữ liệu bất thường"
                review_note += " LOF xác định là điểm ngoại lai cục bộ."
    except Exception as e:
        print(f"Lỗi khi phân tích bất thường: {e}")

    approval_status = "APPROVED"
    if fraud_risk >= 70:
        approval_status = "REJECTED"
    elif fraud_risk >= 40:
        approval_status = "PENDING"

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
    Tải lịch sử hoạt động của người dùng từ tệp CSV.

    Args:
        user_id: ID của người dùng.

    Returns:
        pd.DataFrame: Lịch sử hoạt động hoặc None nếu không có.
    """
    history_path = os.getenv('USER_HISTORY_PATH', 'user_history')
    user_file = os.path.join(history_path, f"user_{user_id}_history.csv")

    try:
        if os.path.exists(user_file):
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
        if len(sys.argv) < 2:
            print("Cách sử dụng: python record_validator.py <chuỗi_json_bản_ghi> [--file]")
            sys.exit(1)

        is_file_path = False
        if len(sys.argv) > 2 and sys.argv[2] == "--file":
            is_file_path = True

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

        result = validate_record(record_json)
        print("\n--- BEGIN JSON RESULT ---")
        print(json.dumps(result, ensure_ascii=False))
        print("--- END JSON RESULT ---")
    except Exception as e:
        print(f"Lỗi không mong đợi: {e}")
        print(json.dumps({
            "approvalStatus": "PENDING",
            "fraudRisk": 50.0,
            "fraudType": "Lỗi hệ thống",
            "reviewNote": f"Lỗi không mong đợi: {str(e)}"
        }, ensure_ascii=False))