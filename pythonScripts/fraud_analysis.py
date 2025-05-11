import pandas as pd
import json
import sys
import os
import warnings
warnings.filterwarnings('ignore')
sys.stdout.reconfigure(encoding='utf-8')

# Import các hàm từ file modal.py
from modal import (
    prepare_marathon_data, analyze_per_user, create_advanced_features,
    classify_marathon_phase, extract_features, detect_anomalies_isolation_forest,
    detect_anomalies_lof, detect_anomalies_kmeans, visualize_results,
    detailed_fraud_analysis, generate_final_report
)

def load_marathon_data(file_path):
    """Đọc dữ liệu từ file CSV"""
    try:
        data = pd.read_csv(file_path)
        print(f"Đã đọc {len(data)} bản ghi từ {file_path}")
        return data
    except Exception as e:
        print(f"Lỗi khi đọc file: {e}")
        return pd.DataFrame()

def analyze_marathon_fraud_with_output(file_path):
    """Phân tích dữ liệu marathon và trả về kết quả dưới dạng JSON"""
    try:
        # Đọc dữ liệu
        marathon_df = load_marathon_data(file_path)

        # Kiểm tra nếu DataFrame rỗng
        if marathon_df.empty:
            return json.dumps({
                "totalRecords": 0,
                "totalFraudRecords": 0,
                "fraudUserIds": [],
                "userRiskScores": {},
                "fraudRecordDetails": []
            }, ensure_ascii=False)

        # Chuẩn bị dữ liệu
        processed_df = prepare_marathon_data(marathon_df)

        # Phân tích theo người dùng
        processed_df, user_stats = analyze_per_user(processed_df)

        # Tạo các đặc trưng nâng cao
        processed_df = create_advanced_features(processed_df)

        # Phân loại giai đoạn marathon
        processed_df = classify_marathon_phase(processed_df)

        # Trích xuất đặc trưng
        try:
            features_scaled, features_df = extract_features(processed_df)
        except ValueError as e:
            print(f"Lỗi khi trích xuất đặc trưng: {e}")
            # Trả về kết quả không có gian lận
            return json.dumps({
                "totalRecords": len(processed_df),
                "totalFraudRecords": 0,
                "fraudUserIds": [],
                "userRiskScores": {},
                "fraudRecordDetails": []
            }, ensure_ascii=False)

        # Phát hiện gian lận bằng nhiều phương pháp
        predictions_dict = {}

        # Kiểm tra số lượng mẫu để điều chỉnh contamination
        n_samples = features_scaled.shape[0]
        base_contamination = 0.05  # Giá trị mặc định

        # Điều chỉnh contamination dựa trên kích thước dữ liệu
        if n_samples < 20:
            adjusted_contamination = max(1/n_samples, base_contamination)
        else:
            adjusted_contamination = base_contamination

        # Isolation Forest - tối ưu với n_estimators=100 thay vì 200
        try:
            if_predictions, if_model = detect_anomalies_isolation_forest(
                features_scaled, contamination=adjusted_contamination
            )
            predictions_dict['isolation_forest'] = if_predictions
        except Exception as e:
            print(f"Lỗi khi sử dụng Isolation Forest: {e}")

        # LocalOutlierFactor
        try:
            lof_predictions, lof_scores = detect_anomalies_lof(
                features_scaled, contamination=adjusted_contamination
            )
            predictions_dict['local_outlier_factor'] = lof_predictions
        except Exception as e:
            print(f"Lỗi khi sử dụng Local Outlier Factor: {e}")

        # KMeans clustering
        try:
            kmeans_predictions, kmeans_model, distances = detect_anomalies_kmeans(
                features_scaled, n_clusters=min(4, n_samples // 2) if n_samples > 3 else 2
            )
            predictions_dict['kmeans'] = kmeans_predictions
        except Exception as e:
            print(f"Lỗi khi sử dụng KMeans: {e}")

        # Kiểm tra xem đã phát hiện được gian lận bằng bất kỳ phương pháp nào chưa
        if not predictions_dict:
            # Nếu không có phương pháp nào thành công, trả về kết quả không có gian lận
            return json.dumps({
                "totalRecords": len(processed_df),
                "totalFraudRecords": 0,
                "fraudUserIds": [],
                "userRiskScores": {},
                "fraudRecordDetails": []
            }, ensure_ascii=False)

        # Tổng hợp kết quả - đã loại bỏ phần tạo biểu đồ
        result_df = visualize_results(processed_df, features_df, predictions_dict)

        # Thêm cột kết luận cuối cùng
        result_df['IsFraud'] = result_df['CombinedFraudFlag']

        # Phân tích chi tiết các trường hợp gian lận
        detailed_fraud_analysis(result_df)

        # Tạo báo cáo cuối cùng
        fraud_cases = result_df[result_df['IsFraud'] == 1]
        user_risk_scores = generate_final_report(result_df, fraud_cases)

        # Chuẩn bị kết quả để trả về dưới dạng JSON
        user_id_col = 'UserId' if 'UserId' in result_df.columns else 'Id'
        id_col = 'Id' if 'Id' in result_df.columns else user_id_col

        # Tạo danh sách các người dùng gian lận
        fraud_users = result_df[result_df['IsFraud'] == 1][user_id_col].unique().tolist()

        # Tạo chi tiết cho mỗi trường hợp gian lận
        fraud_record_details = []

        if len(fraud_cases) > 0:
            for _, fraud_record in fraud_cases.iterrows():
                # Lấy các thông số hoạt động
                activity_data = {}
                for col in ['TotalSteps', 'TotalDistance', 'TimeTaken', 'AvgSpeed', 'DistancePerStep', 'Timestamp']:
                    if col in fraud_record and not pd.isna(fraud_record[col]):
                        activity_data[col] = convert_value_for_json(fraud_record[col])

                # Thêm điểm bất thường nếu có
                for method in ['isolation_forest', 'local_outlier_factor', 'kmeans']:
                    col = f'FraudFlag_{method}'
                    if col in fraud_record:
                        activity_data[f'anomaly_{method}'] = int(fraud_record[col])

                # Thêm các điểm độ lệch
                for col in ['StepDeviation', 'SpeedDeviation', 'DistPerStepDeviation']:
                    if col in fraud_record and not pd.isna(fraud_record[col]):
                        activity_data[col] = convert_value_for_json(fraud_record[col])

                # Tính điểm rủi ro cho bản ghi này
                risk_score = 0
                if user_risk_scores and str(fraud_record[user_id_col]) in user_risk_scores:
                    risk_score = user_risk_scores[str(fraud_record[user_id_col])].get('risk_score', 0)

                # Xác định loại gian lận
                fraud_type = fraud_record.get('FraudType', 'Dữ liệu bất thường')

                # Tạo chi tiết bản ghi gian lận
                fraud_detail = {
                    "id": str(fraud_record[id_col]),
                    "userId": str(fraud_record[user_id_col]),
                    "fraudType": fraud_type,
                    "riskScore": convert_value_for_json(risk_score),
                    "activityData": activity_data
                }

                fraud_record_details.append(fraud_detail)

        # Chuyển đổi các giá trị float trong user_risk_scores
        user_risk_scores_clean = convert_values_for_json(user_risk_scores)

        # Tạo dictionary kết quả
        result = {
            "totalRecords": len(result_df),
            "totalFraudRecords": len(fraud_cases),
            "fraudUserIds": fraud_users,
            "userRiskScores": user_risk_scores_clean,
            "fraudRecordDetails": fraud_record_details
        }

        # Đảm bảo tất cả các key đều là chuỗi
        result = convert_keys(result)

        # Trả về kết quả dưới dạng JSON
        return json.dumps(result, ensure_ascii=False)

    except Exception as e:
        # Bắt tất cả các ngoại lệ khác và trả về JSON lỗi
        print(f"Lỗi không mong đợi: {e}")
        return json.dumps({
            "error": str(e),
            "totalRecords": 0,
            "totalFraudRecords": 0,
            "fraudUserIds": [],
            "userRiskScores": {},
            "fraudRecordDetails": []
        }, ensure_ascii=False)

def convert_keys(obj):
    """Chuyển đổi tất cả các khóa trong dictionary thành chuỗi"""
    if isinstance(obj, dict):
        return {str(k): convert_keys(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [convert_keys(i) for i in obj]
    else:
        return obj

def convert_values_for_json(obj):
    """Chuyển đổi các giá trị float thành có thể serialize bằng JSON"""
    if isinstance(obj, dict):
        return {k: convert_values_for_json(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [convert_values_for_json(i) for i in obj]
    elif isinstance(obj, float):
        return round(obj, 6)  # Làm tròn số float để tránh lỗi precision
    elif isinstance(obj, pd.Timestamp):
        return str(obj)  # Chuyển timestamp thành chuỗi
    else:
        return obj

def convert_value_for_json(value):
    """Chuyển đổi một giá trị đơn lẻ thành định dạng có thể serialize bằng JSON"""
    if isinstance(value, float):
        return round(value, 6)
    elif isinstance(value, pd.Timestamp):
        return str(value)
    else:
        return value

if __name__ == "__main__":
    try:
        if len(sys.argv) < 2:
            print("Cách sử dụng: python fraud_analysis.py <đường_dẫn_file_csv>")
            # Trả về JSON mặc định khi không có tham số
            print(json.dumps({
                "totalRecords": 0,
                "totalFraudRecords": 0,
                "fraudUserIds": [],
                "userRiskScores": {},
                "fraudRecordDetails": []
            }, ensure_ascii=False))
            sys.exit(1)

        # Lấy đường dẫn file từ tham số dòng lệnh
        file_path = sys.argv[1]

        # Kiểm tra xem file có tồn tại không
        if not os.path.exists(file_path):
            print(f"Không tìm thấy file: {file_path}")
            # Trả về JSON lỗi
            print(json.dumps({
                "error": f"File not found: {file_path}",
                "totalRecords": 0,
                "totalFraudRecords": 0,
                "fraudUserIds": [],
                "userRiskScores": {},
                "fraudRecordDetails": []
            }, ensure_ascii=False))
            sys.exit(1)

        # Chạy phân tích và in kết quả JSON
        result_json = analyze_marathon_fraud_with_output(file_path)
        print("\n--- BEGIN JSON RESULT ---")  # Marker để dễ dàng trích xuất JSON
        print(result_json)
        print("--- END JSON RESULT ---")  # Marker để dễ dàng trích xuất JSON
    except Exception as e:
        # Bắt tất cả các ngoại lệ và trả về JSON lỗi
        print(f"Lỗi không mong đợi: {e}")
        print(json.dumps({
            "error": str(e),
            "totalRecords": 0,
            "totalFraudRecords": 0,
            "fraudUserIds": [],
            "userRiskScores": {},
            "fraudRecordDetails": []
        }, ensure_ascii=False))