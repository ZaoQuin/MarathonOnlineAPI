import pandas as pd
import json
import sys
import os
import warnings
warnings.filterwarnings('ignore')
sys.stdout.reconfigure(encoding='utf-8')

from module import (
    prepare_marathon_data, analyze_per_user, extract_features,
    detect_anomalies_isolation_forest, detect_anomalies_lof
)

def validate_record(record_json):
    try:
        record_json = record_json.strip()
        if not record_json:
            raise ValueError("Empty JSON string")

        try:
            record = json.loads(record_json)
        except json.JSONDecodeError as e:
            record_json_fixed = fix_json_string(record_json)
            record = json.loads(record_json_fixed)

        if not isinstance(record, dict):
            raise ValueError("JSON must be an object/dictionary")

        record_df = pd.DataFrame([{
            'Id': safe_get(record, 'id', ''),
            'UserId': safe_get_nested(record, ['user', 'id'], ''),
            'TotalSteps': safe_get_numeric(record, 'steps', 0),
            'TotalDistance': safe_get_numeric(record, 'distance', 0.0),
            'TimeTaken': safe_get_numeric(record, 'timeTaken', 0),
            'AvgSpeed': safe_get_numeric(record, 'avgSpeed', 0.0),
            'EndTime': safe_get(record, 'endTime', ''),
            'heartRate': safe_get_numeric(record, 'heartRate', None, allow_none=True)
        }])

        if not validate_basic_conditions(record_df):
            return {
                "approvalStatus": "REJECTED",
                "fraudRisk": 100.0,
                "fraudType": "Dữ liệu cần kiểm tra lại",
                "reviewNote": "Một số thông tin chạy bộ cần được xem xét lại để đảm bảo chính xác. Vui lòng kiểm tra lại số bước, khoảng cách, thời gian và nhịp tim của bạn."
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

    except json.JSONDecodeError as e:
        error_msg = f"Định dạng dữ liệu cần điều chỉnh: {str(e)}"
        print(f"Thông tin xử lý: {error_msg}")
        return {
            "approvalStatus": "PENDING",
            "fraudRisk": 50.0,
            "fraudType": "Cần điều chỉnh định dạng",
            "reviewNote": "Dữ liệu cần được định dạng lại để xử lý chính xác hơn."
        }
    except Exception as e:
        error_msg = f"Cần kiểm tra lại thông tin bản ghi: {str(e)}"
        print(error_msg)
        return {
            "approvalStatus": "PENDING",
            "fraudRisk": 50.0,
            "fraudType": "Cần xem xét thêm",
            "reviewNote": "Thông tin cần được xem xét thêm để đảm bảo chính xác."
        }

def fix_json_string(json_str):
    json_str = json_str.strip()

    import re
    json_str = re.sub(r"'([^']*)':", r'"\1":', json_str)
    json_str = re.sub(r":\s*'([^']*)'", r': "\1"', json_str)

    json_str = re.sub(r'(\w+):', r'"\1":', json_str)

    json_str = re.sub(r',\s*}', '}', json_str)
    json_str = re.sub(r',\s*]', ']', json_str)

    return json_str

def safe_get(data, key, default=None):
    return data.get(key, default) if isinstance(data, dict) else default

def safe_get_nested(data, keys, default=None):
    current = data
    for key in keys:
        if isinstance(current, dict) and key in current:
            current = current[key]
        else:
            return default
    return current

def safe_get_numeric(data, key, default=0, allow_none=False):
    value = safe_get(data, key, default)
    if value is None and allow_none:
        return None
    try:
        return float(value) if value is not None else default
    except (ValueError, TypeError):
        return default

def validate_basic_conditions(record_df):
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
    steps = processed_df['TotalSteps'].iloc[0]
    distance = processed_df['TotalDistance'].iloc[0]
    time_taken = processed_df['TimeTaken'].iloc[0]
    heart_rate = processed_df['heartRate'].iloc[0] if 'heartRate' in processed_df.columns and pd.notna(processed_df['heartRate'].iloc[0]) else None

    speed = distance / (time_taken / 60) if time_taken > 0 else 0
    distance_per_step = (distance * 1000) / steps if steps > 0 else 0
    fraud_risk = 0
    fraud_type = None
    review_note = "Tuyệt vời! Kết quả chạy bộ của bạn trông rất tốt."

    if speed > 20:
        fraud_risk = max(fraud_risk, 90)
        fraud_type = "Tốc độ cần xác nhận"
        review_note = f"Wow! Tốc độ {speed:.2f}km/h thật ấn tượng. Hãy giúp chúng tôi xác nhận bạn thực sự chạy bộ để ghi nhận thành tích này nhé!"
    elif speed > 15:
        fraud_risk = max(fraud_risk, 70)
        fraud_type = "Tốc độ xuất sắc"
        review_note = f"Tốc độ {speed:.2f}km/h rất tuyệt! Chúng tôi chỉ cần xác minh thêm để đảm bảo ghi nhận chính xác thành tích của bạn."

    if distance_per_step > 1.5:
        fraud_risk = max(fraud_risk, 80)
        fraud_type = "Chiều dài bước cần kiểm tra"
        review_note = f"Chiều dài bước ({distance_per_step:.2f}m) của bạn khá đặc biệt. Hãy giúp chúng tôi xác nhận để ghi nhận chính xác thành tích này!"
    elif distance_per_step > 1.0:
        fraud_risk = max(fraud_risk, 60)
        fraud_type = "Chiều dài bước đặc biệt"
        review_note = f"Chiều dài bước ({distance_per_step:.2f}m) của bạn khá ấn tượng. Chúng tôi sẽ xem xét để ghi nhận chính xác."

    steps_per_minute = steps / time_taken if time_taken > 0 else 0
    if steps_per_minute > 250:
        fraud_risk = max(fraud_risk, 75)
        fraud_type = "Nhịp độ bước cần xác nhận"
        review_note = f"Nhịp độ {steps_per_minute:.0f} bước/phút thật tuyệt vời! Hãy giúp chúng tôi xác nhận để ghi nhận thành tích này."

    if heart_rate is not None and heart_rate > 0:
        if heart_rate < 60 and steps > 10000:
            fraud_risk = max(fraud_risk, 85)
            fraud_type = "Nhịp tim cần kiểm tra"
            review_note = f"Nhịp tim {heart_rate:.1f} bpm với {steps} bước khá đặc biệt. Hãy kiểm tra lại thiết bị đo nhịp tim để đảm bảo chính xác nhé!"

    approval_status = "APPROVED"
    if fraud_risk >= 70:
        approval_status = "REJECTED"
    elif fraud_risk >= 40:
        approval_status = "PENDING"

    return {
        "approvalStatus": approval_status,
        "fraudRisk": float(fraud_risk),
        "fraudType": fraud_type if fraud_type else "Hoàn hảo",
        "reviewNote": review_note
    }

def validate_with_user_history(analysis_df, new_record_index, user_stats):
    new_record = analysis_df.iloc[new_record_index]
    user_id = new_record['UserId']
    fraud_risk = 0
    fraud_type = None
    review_note = "Tuyệt vời! Kết quả chạy bộ của bạn rất ổn định và tự nhiên."

    if 'StepDeviation' in analysis_df.columns and abs(new_record['StepDeviation']) > 3:
        fraud_risk = max(fraud_risk, 75)
        fraud_type = "Số bước khác thường"
        review_note = f"Số bước hôm nay khác khá nhiều so với thói quen thường ngày ({abs(new_record['StepDeviation']):.2f} lần). Điều này có bình thường không?"

    if 'SpeedDeviation' in analysis_df.columns and abs(new_record['SpeedDeviation']) > 3:
        fraud_risk = max(fraud_risk, 80)
        fraud_type = "Tốc độ bất ngờ"
        review_note = f"Tốc độ hôm nay thay đổi khá nhiều so với lịch sử ({abs(new_record['SpeedDeviation']):.2f} lần). Bạn có tập luyện đặc biệt gì không?"

    if 'DistPerStepDeviation' in analysis_df.columns and abs(new_record['DistPerStepDeviation']) > 3:
        fraud_risk = max(fraud_risk, 85)
        fraud_type = "Kiểu chạy khác lạ"
        review_note = f"Kiểu chạy hôm nay có vẻ khác so với thường ngày ({abs(new_record['DistPerStepDeviation']):.2f} lần). Có thể bạn thay đổi cách chạy?"

    if ('heartRate' in analysis_df.columns and pd.notna(new_record['heartRate']) and
            'HeartRateDeviation' in analysis_df.columns and pd.notna(new_record['HeartRateDeviation'])):
        if abs(new_record['HeartRateDeviation']) > 3:
            fraud_risk = max(fraud_risk, 80)
            fraud_type = "Nhịp tim khác thường"
            review_note = f"Nhịp tim hôm nay khác khá nhiều so với thường ngày ({abs(new_record['HeartRateDeviation']):.2f} lần). Bạn có cảm thấy khác lạ gì không?"

    if user_id in user_stats and 'step_correlations' in user_stats[user_id]:
        if user_stats[user_id]['step_correlations'].get('TotalDistance', 1.0) < 0.5:
            fraud_risk = max(fraud_risk, 70)
            fraud_type = "Mẫu chạy khác lạ"
            review_note = "Mối quan hệ giữa số bước và khoảng cách hôm nay có vẻ khác so với thường ngày. Có thể bạn chạy ở địa hình mới?"

    try:
        features_scaled, _ = extract_features(analysis_df)
        if len(features_scaled) >= 5:
            if_predictions, _ = detect_anomalies_isolation_forest(features_scaled, contamination=0.1)
            if if_predictions[new_record_index] == 1:
                fraud_risk = max(fraud_risk, 70)
                if fraud_type is None:
                    fraud_type = "Mẫu chạy đặc biệt"
                review_note += " Các chỉ số hôm nay có một số điểm đặc biệt so với thường ngày."

            lof_predictions, _ = detect_anomalies_lof(features_scaled, contamination=0.1)
            if lof_predictions[new_record_index] == 1:
                fraud_risk = max(fraud_risk, 65)
                if fraud_type is None:
                    fraud_type = "Dữ liệu đặc biệt"
                review_note += " Một số chỉ số cần được xem xét thêm."
    except Exception as e:
        print(f"Thông tin xử lý: Đang phân tích dữ liệu - {e}")

    approval_status = "APPROVED"
    if fraud_risk >= 70:
        approval_status = "REJECTED"
    elif fraud_risk >= 40:
        approval_status = "PENDING"

    if fraud_risk == 0:
        fraud_type = "Hoàn hảo"

    return {
        "approvalStatus": approval_status,
        "fraudRisk": float(fraud_risk),
        "fraudType": fraud_type,
        "reviewNote": review_note
    }

def load_user_history(user_id, days=7):
    try:
        from datetime import datetime, timedelta
        end_date = datetime.now()
        start_date = end_date - timedelta(days=days)
        api_url = (
                os.getenv('API_URL', 'http://localhost:8080/api/v1/record/user') +
                f'/{user_id}/history?startDate={start_date.strftime("%Y-%m-%dT%H:%M:%S")}&endDate={end_date.strftime("%Y-%m-%dT%H:%M:%S")}'
        )
        print(f"Đang tải lịch sử chạy bộ của bạn: {api_url}")
        response = requests.get(api_url, timeout=10)
        response.raise_for_status()

        records = response.json()
        if not records:
            print(f"Chưa có lịch sử chạy bộ cho người dùng {user_id} trong {days} ngày qua")
            return None

        user_history = pd.DataFrame([{
            'Id': safe_get(record, 'id', ''),
            'UserId': safe_get_nested(record, ['user', 'id'], ''),
            'TotalSteps': safe_get_numeric(record, 'steps', 0),
            'TotalDistance': safe_get_numeric(record, 'distance', 0.0),
            'TimeTaken': safe_get_numeric(record, 'timeTaken', 0),
            'AvgSpeed': safe_get_numeric(record, 'avgSpeed', 0.0),
            'Timestamp': safe_get(record, 'endTime', ''),
            'heartRate': safe_get_numeric(record, 'heartRate', None, allow_none=True)
        } for record in records])

        print(f"Đã tải {len(user_history)} bản ghi lịch sử chạy bộ của người dùng {user_id} trong {days} ngày qua")
        return user_history

    except requests.RequestException as e:
        print(f"Không thể tải lịch sử chạy bộ: {e}")
        return None
    except Exception as e:
        print(f"Có vấn đề khi xử lý lịch sử: {e}")
        return None

if __name__ == "__main__":
    try:
        if len(sys.argv) < 2:
            print("Hướng dẫn sử dụng: python record_validator.py <dữ_liệu_json> [--file] [--userId <user_id>]")
            sys.exit(1)

        is_file_path = False
        user_id = None

        args = sys.argv[1:]
        if "--file" in args:
            is_file_path = True
            args.remove("--file")

        if "--userId" in args:
            user_id_index = args.index("--userId")
            if user_id_index + 1 < len(args):
                user_id = args[user_id_index + 1]
                args.remove("--userId")
                args.remove(user_id)

        if not args:
            print("Lưu ý: Vui lòng cung cấp dữ liệu JSON")
            sys.exit(1)

        if is_file_path:
            file_path = args[0]
            print(f"Đang đọc dữ liệu từ tệp: {file_path}")
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    record_json = f.read().strip()
            except FileNotFoundError:
                print(f"Không tìm thấy tệp: {file_path}")
                sys.exit(1)
            except Exception as e:
                print(f"Có vấn đề khi đọc tệp: {e}")
                sys.exit(1)
        else:
            record_json = args[0]

        if not record_json:
            print("Lưu ý: Dữ liệu JSON trống")
            sys.exit(1)

        result = validate_record(record_json)
        print("\n--- KẾT QUẢ PHÂN TÍCH ---")
        print(json.dumps(result, ensure_ascii=False, indent=2))
        print("--- HẾT KẾT QUẢ ---")

    except Exception as e:
        print(f"Có vấn đề không mong đợi: {e}")
        error_result = {
            "approvalStatus": "PENDING",
            "fraudRisk": 50.0,
            "fraudType": "Cần hỗ trợ kỹ thuật",
            "reviewNote": f"Hệ thống gặp vấn đề và cần được hỗ trợ: {str(e)}"
        }
        print("\n--- KẾT QUẢ PHÂN TÍCH ---")
        print(json.dumps(error_result, ensure_ascii=False, indent=2))
        print("--- HẾT KẾT QUẢ ---")