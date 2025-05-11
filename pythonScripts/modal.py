import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
from sklearn.neighbors import LocalOutlierFactor
import warnings
warnings.filterwarnings('ignore')

# Xử lý và chuẩn bị dữ liệu cho định dạng marathon
def prepare_marathon_data(marathon_df):
    # Kiểm tra và xử lý cột Timestamp nếu có
    if 'Timestamp' in marathon_df.columns:
        marathon_df['Timestamp'] = pd.to_datetime(marathon_df['Timestamp'])
        # Thêm thông tin về ngày trong tuần
        marathon_df['DayOfWeek'] = marathon_df['Timestamp'].dt.dayofweek
        marathon_df['Weekend'] = marathon_df['DayOfWeek'].apply(lambda x: 1 if x >= 5 else 0)

    # Tính tỷ lệ giữa quãng đường và số bước
    if all(col in marathon_df.columns for col in ['TotalDistance', 'TotalSteps']):
        marathon_df['DistancePerStep'] = np.where(
            marathon_df['TotalSteps'] > 0,
            marathon_df['TotalDistance'] / marathon_df['TotalSteps'],
            0
        )

    # Tính tốc độ trung bình từ TimeTaken nếu chưa có AvgSpeed
    if 'TimeTaken' in marathon_df.columns and 'TotalDistance' in marathon_df.columns and 'AvgSpeed' not in marathon_df.columns:
        marathon_df['AvgSpeed'] = np.where(
            marathon_df['TimeTaken'] > 0,
            marathon_df['TotalDistance'] / (marathon_df['TimeTaken'] / 60),  # km/h
            0
        )

    # Thêm cột VeryActiveMinutes từ TimeTaken
    if 'TimeTaken' in marathon_df.columns and 'VeryActiveMinutes' not in marathon_df.columns:
        marathon_df['VeryActiveMinutes'] = marathon_df['TimeTaken']

    # Thêm cột VeryActiveDistance từ TotalDistance
    if 'TotalDistance' in marathon_df.columns and 'VeryActiveDistance' not in marathon_df.columns:
        marathon_df['VeryActiveDistance'] = marathon_df['TotalDistance']

    return marathon_df.copy()

# Phân tích theo từng người dùng
def analyze_per_user(df):
    user_stats = {}
    user_id_col = 'UserId' if 'UserId' in df.columns else 'Id'
    user_ids = df[user_id_col].unique()

    for user_id in user_ids:
        user_data = df[df[user_id_col] == user_id]
        if len(user_data) >= 2:  # Chỉ phân tích người dùng có ít nhất 2 bản ghi
            user_stats[user_id] = {}

            # Tính toán thống kê cho các cột có sẵn
            for col in ['TotalSteps', 'TotalDistance', 'AvgSpeed', 'DistancePerStep']:
                if col in user_data.columns:
                    user_stats[user_id][f'avg_{col.lower()}'] = user_data[col].mean()
                    user_stats[user_id][f'std_{col.lower()}'] = user_data[col].std()

            user_stats[user_id]['record_count'] = len(user_data)

    # Xác định mức độ bất thường của từng bản ghi
    for col, prefix in zip(
            ['TotalSteps', 'AvgSpeed', 'DistancePerStep'],
            ['StepDeviation', 'SpeedDeviation', 'DistPerStepDeviation']
    ):
        if col in df.columns:
            df[prefix] = 0.0

            for user_id, stats in user_stats.items():
                user_indices = df[df[user_id_col] == user_id].index
                if len(user_indices) > 0 and f'avg_{col.lower()}' in stats and f'std_{col.lower()}' in stats:
                    df.loc[user_indices, prefix] = (
                                                           df.loc[user_indices, col] - stats[f'avg_{col.lower()}']
                                                   ) / (stats[f'std_{col.lower()}'] + 1e-6)

    return df, user_stats

# Trích xuất đặc trưng để phát hiện bất thường
def extract_features(df):
    # Xác định các đặc trưng có sẵn trong dữ liệu
    available_basic_features = [col for col in [
        'TotalSteps', 'TotalDistance', 'VeryActiveDistance',
        'VeryActiveMinutes', 'AvgSpeed', 'DistancePerStep'
    ] if col in df.columns]

    available_advanced_features = [col for col in [
        'Weekend', 'DayOfWeek'
    ] if col in df.columns]

    available_user_features = [col for col in [
        'StepDeviation', 'SpeedDeviation', 'DistPerStepDeviation'
    ] if col in df.columns]

    # Kết hợp tất cả đặc trưng có sẵn
    all_available_features = available_basic_features + available_advanced_features + available_user_features

    if not all_available_features:
        raise ValueError("Không có đặc trưng nào khả dụng để phân tích")

    features = df[all_available_features].copy()
    features = features.fillna(0)

    # Chuẩn hóa dữ liệu
    scaler = StandardScaler()
    features_scaled = scaler.fit_transform(features)

    return features_scaled, features

# Mô hình phát hiện bất thường - Isolation Forest
def detect_anomalies_isolation_forest(features_scaled, contamination=0.05):
    model = IsolationForest(contamination=contamination, random_state=42, n_estimators=100)
    predictions = model.fit_predict(features_scaled)
    # Chuyển -1 (bất thường) thành 1 (gian lận) và 1 (bình thường) thành 0 (không gian lận)
    fraud_predictions = np.where(predictions == -1, 1, 0)
    return fraud_predictions, model

# Mô hình phân cụm để phát hiện nhóm dữ liệu bất thường
def detect_anomalies_kmeans(features_scaled, n_clusters=4):
    # Đảm bảo số cụm không lớn hơn số mẫu
    n_samples = features_scaled.shape[0]
    n_clusters = min(n_clusters, n_samples - 1)

    kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
    cluster_labels = kmeans.fit_predict(features_scaled)

    # Tính khoảng cách từ mỗi điểm đến tâm cụm
    distances = np.zeros(len(features_scaled))
    for i in range(len(features_scaled)):
        cluster = cluster_labels[i]
        distances[i] = np.linalg.norm(features_scaled[i] - kmeans.cluster_centers_[cluster])

    # Xác định ngưỡng cho khoảng cách bất thường
    threshold = np.percentile(distances, 95)  # 5% cao nhất

    # Đánh dấu bản ghi có khoảng cách lớn là gian lận
    fraud_predictions = np.where(distances > threshold, 1, 0)

    return fraud_predictions, kmeans, distances

# Sử dụng Local Outlier Factor để phát hiện bất thường
def detect_anomalies_lof(features_scaled, contamination=0.05):
    # Đảm bảo số n_neighbors không lớn hơn số mẫu
    n_samples = features_scaled.shape[0]
    n_neighbors = min(20, n_samples - 1)

    lof = LocalOutlierFactor(n_neighbors=n_neighbors, contamination=contamination)
    predictions = lof.fit_predict(features_scaled)
    # Chuyển -1 (bất thường) thành 1 (gian lận) và 1 (bình thường) thành 0 (không gian lận)
    fraud_predictions = np.where(predictions == -1, 1, 0)

    # Tính điểm bất thường
    lof_scores = -lof.negative_outlier_factor_

    return fraud_predictions, lof_scores

# Chức năng visualize_results đã được sửa đổi để loại bỏ phần tạo biểu đồ
def visualize_results(df, features, predictions_dict):
    # Kết hợp các phương pháp phát hiện
    df['CombinedFraudFlag'] = 0
    fraud_count_per_record = np.zeros(len(df))

    for method_name, predictions in predictions_dict.items():
        # Skip non-prediction items in the dictionary
        if method_name.endswith('_model') or not isinstance(predictions, np.ndarray):
            continue

        # Make sure predictions are numeric arrays
        try:
            predictions_array = np.array(predictions, dtype=np.int32)
            df[f'FraudFlag_{method_name}'] = predictions_array
            fraud_count_per_record += predictions_array
        except (TypeError, ValueError) as e:
            print(f"Warning: Could not process predictions for {method_name}: {e}")
            continue

    # Gắn nhãn gian lận nếu ít nhất 2 phương pháp cùng phát hiện
    df['CombinedFraudFlag'] = np.where(fraud_count_per_record >= 2, 1, 0)

    return df

# Phân loại giai đoạn hoạt động của marathon
def classify_marathon_phase(df):
    if 'VeryActiveMinutes' in df.columns:
        df['MarathonPhase'] = pd.cut(
            df['VeryActiveMinutes'],
            bins=[0, 60, 120, 180, 300, float('inf')],
            labels=['Start', 'Early', 'Middle', 'Late', 'Extended']
        )
    return df

# Tạo các đặc trưng bổ sung
def create_advanced_features(df):
    if all(x in df.columns for x in ['Calories', 'TotalDistance']):
        df['EnergyDensity'] = df['Calories'] / (df['TotalDistance'] + 0.001)

    if all(x in df.columns for x in ['TotalSteps', 'VeryActiveMinutes']):
        df['StepConsistency'] = df['TotalSteps'] / (df['VeryActiveMinutes'] + 0.001)

    return df

# Phân tích chi tiết các trường hợp gian lận
def detailed_fraud_analysis(df):
    fraud_cases = df[df['IsFraud'] == 1]
    print(f"\n=== Phân tích chi tiết {len(fraud_cases)} trường hợp gian lận ===")

    if len(fraud_cases) == 0:
        print("Không có trường hợp gian lận được phát hiện.")
        return

    # Phân loại các loại gian lận
    df['FraudType'] = "Unknown"

    if 'AvgSpeed' in df.columns:
        # 1. Sử dụng phương tiện (tốc độ cao bất thường)
        vehicle_fraud_mask = (df['AvgSpeed'] > 12) & (df['IsFraud'] == 1)
        df.loc[vehicle_fraud_mask, 'FraudType'] = "Sử dụng phương tiện"
    else:
        vehicle_fraud_mask = pd.Series(False, index=df.index)

    # 2. Đi tắt đường
    shortcut_fraud_mask = pd.Series(False, index=df.index)
    if all(x in df.columns for x in ['SpeedDeviation', 'DistPerStepDeviation']):
        shortcut_fraud_mask = (df['SpeedDeviation'] > 2) & (df['DistPerStepDeviation'] > 2) & (df['IsFraud'] == 1)
        df.loc[shortcut_fraud_mask & ~vehicle_fraud_mask, 'FraudType'] = "Đi tắt đường"

    # 3. Khai báo sai
    step_fraud_mask = pd.Series(False, index=df.index)
    if all(x in df.columns for x in ['DistancePerStep']):
        step_fraud_mask = (df['DistancePerStep'] > 0.001) & (df['IsFraud'] == 1) & (~vehicle_fraud_mask)
        if 'DistPerStepDeviation' in df.columns:
            step_fraud_mask = step_fraud_mask & (df['DistPerStepDeviation'] > 2)
        df.loc[step_fraud_mask & ~shortcut_fraud_mask & ~vehicle_fraud_mask, 'FraudType'] = "Khai báo sai số bước"

    # 4. Dữ liệu giả
    unknown_fraud_mask = (df['IsFraud'] == 1) & (df['FraudType'] == "Unknown")
    df.loc[unknown_fraud_mask, 'FraudType'] = "Dữ liệu bất thường"

    # Thống kê theo loại gian lận
    fraud_type_counts = df[df['IsFraud'] == 1]['FraudType'].value_counts()
    print("\nPhân loại các trường hợp gian lận:")
    for fraud_type, count in fraud_type_counts.items():
        print(f"- {fraud_type}: {count} trường hợp")

    # Phân tích theo người dùng
    user_id_col = 'UserId' if 'UserId' in df.columns else 'Id'
    fraud_users = df[df['IsFraud'] == 1][user_id_col].unique()
    print(f"\nSố người dùng có dấu hiệu gian lận: {len(fraud_users)}")

    for user_id in fraud_users:
        user_fraud = df[(df[user_id_col] == user_id) & (df['IsFraud'] == 1)]
        fraud_types = user_fraud['FraudType'].value_counts()

        print(f"\nNgười dùng {user_id}:")
        print(f"- Tổng số hoạt động: {len(df[df[user_id_col] == user_id])}")
        print(f"- Số hoạt động gian lận: {len(user_fraud)} ({len(user_fraud)/len(df[df[user_id_col] == user_id])*100:.1f}%)")
        for fraud_type, count in fraud_types.items():
            print(f"  + {fraud_type}: {count} trường hợp")

        # Thông số trung bình
        print("- Thông số trung bình trong các hoạt động gian lận:")
        for col, format_str in [
            ('AvgSpeed', "  + Tốc độ: {:.2f} km/h"),
            ('DistancePerStep', "  + Khoảng cách/bước: {:.5f} km"),
            ('TotalSteps', "  + Số bước: {:.0f}"),
            ('TotalDistance', "  + Quãng đường: {:.2f} km")
        ]:
            if col in user_fraud.columns:
                print(format_str.format(user_fraud[col].mean()))

# Tạo báo cáo cuối cùng
def generate_final_report(df, fraud_cases):
    total_records = len(df)
    user_id_col = 'UserId' if 'UserId' in df.columns else 'Id'
    total_users = len(df[user_id_col].unique())
    fraud_records = len(fraud_cases)
    fraud_users = len(fraud_cases[user_id_col].unique()) if fraud_records > 0 else 0

    print("\n=== BÁO CÁO PHÁT HIỆN GIAN LẬN ===")
    print(f"Tổng số bản ghi phân tích: {total_records}")
    print(f"Tổng số người dùng: {total_users}")
    print(f"Số bản ghi gian lận: {fraud_records} ({fraud_records/total_records*100:.2f}%)")
    print(f"Số người dùng gian lận: {fraud_users} ({fraud_users/total_users*100:.2f}%)")

    # Phân loại theo loại gian lận
    if fraud_records > 0:
        print("\nThống kê theo loại gian lận:")
        fraud_types = fraud_cases['FraudType'].value_counts()
        for fraud_type, count in fraud_types.items():
            print(f"- {fraud_type}: {count} trường hợp ({count/fraud_records*100:.2f}%)")

    # Tạo bảng điểm đánh giá rủi ro cho từng người dùng
    user_risk_scores = {}
    for user_id in df[user_id_col].unique():
        user_data = df[df[user_id_col] == user_id]
        fraud_data = user_data[user_data['IsFraud'] == 1]

        # Tính điểm rủi ro dựa trên tỷ lệ gian lận và loại gian lận
        fraud_ratio = len(fraud_data) / len(user_data) if len(user_data) > 0 else 0

        # Trọng số cho từng loại gian lận
        fraud_weights = {
            "Sử dụng phương tiện": 1.0,
            "Đi tắt đường": 0.8,
            "Khai báo sai số bước": 0.6,
            "Dữ liệu bất thường": 0.4
        }

        # Điểm rủi ro ban đầu dựa trên tỷ lệ gian lận
        risk_score = fraud_ratio * 100

        # Điều chỉnh dựa trên loại gian lận nghiêm trọng nhất
        if len(fraud_data) > 0:
            fraud_type_counts = fraud_data['FraudType'].value_counts()
            if len(fraud_type_counts) > 0:
                worst_fraud_type = fraud_type_counts.index[0]
                worst_fraud_weight = fraud_weights.get(worst_fraud_type, 0.5)
                risk_score = risk_score * (1 + worst_fraud_weight)

        # Giới hạn điểm rủi ro từ 0-100
        risk_score = min(100, max(0, risk_score))

        # Phân loại mức độ rủi ro
        risk_level = "Thấp"
        if risk_score >= 70:
            risk_level = "Cao"
        elif risk_score >= 40:
            risk_level = "Trung bình"

        user_risk_scores[user_id] = {
            "risk_score": risk_score,
            "risk_level": risk_level,
            "fraud_count": len(fraud_data),
            "total_activities": len(user_data),
            "fraud_ratio": fraud_ratio
        }

    # In báo cáo rủi ro cho từng người dùng
    print("\nBáo cáo rủi ro theo người dùng:")
    for user_id, risk_data in user_risk_scores.items():
        if risk_data['fraud_count'] > 0:
            print(f"- Người dùng {user_id}:")
            print(f"  + Điểm rủi ro: {risk_data['risk_score']:.1f}/100 (Mức: {risk_data['risk_level']})")
            print(f"  + Số hoạt động gian lận: {risk_data['fraud_count']}/{risk_data['total_activities']} ({risk_data['fraud_ratio']*100:.1f}%)")

    return user_risk_scores