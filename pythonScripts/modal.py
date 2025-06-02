import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
from sklearn.neighbors import LocalOutlierFactor
import warnings
warnings.filterwarnings('ignore')

def prepare_marathon_data(marathon_df):
    """
    Chuẩn bị dữ liệu marathon, chỉ xử lý heartRate khi có giá trị hợp lệ.

    Args:
        marathon_df (pd.DataFrame): DataFrame chứa dữ liệu marathon.

    Returns:
        pd.DataFrame: DataFrame đã được xử lý với các đặc trưng bổ sung.
    """
    # Kiểm tra và xử lý cột Timestamp nếu có
    if 'Timestamp' in marathon_df.columns:
        marathon_df['Timestamp'] = pd.to_datetime(marathon_df['Timestamp'])
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
    if all(col in marathon_df.columns for col in ['TimeTaken', 'TotalDistance']) and 'AvgSpeed' not in marathon_df.columns:
        marathon_df['AvgSpeed'] = np.where(
            marathon_df['TimeTaken'] > 0,
            marathon_df['TotalDistance'] / (marathon_df['TimeTaken'] / 60),  # km/h
            0
        )

    # Thêm cột VeryActiveMinutes và VeryActiveDistance
    if 'TimeTaken' in marathon_df.columns and 'VeryActiveMinutes' not in marathon_df.columns:
        marathon_df['VeryActiveMinutes'] = marathon_df['TimeTaken']
    if 'TotalDistance' in marathon_df.columns and 'VeryActiveDistance' not in marathon_df.columns:
        marathon_df['VeryActiveDistance'] = marathon_df['TotalDistance']

    # Xử lý heartRate chỉ khi có giá trị hợp lệ
    if 'heartRate' in marathon_df.columns and marathon_df['heartRate'].notna().any():
        # Tính đặc trưng bổ sung từ heartRate, chỉ cho các hàng có heartRate hợp lệ
        marathon_df['HeartRatePerStep'] = np.where(
            (marathon_df['TotalSteps'] > 0) & (marathon_df['heartRate'].notna()),
            marathon_df['heartRate'] / marathon_df['TotalSteps'],
            np.nan
        )
        marathon_df['HeartRatePerSpeed'] = np.where(
            (marathon_df['AvgSpeed'] > 0) & (marathon_df['heartRate'].notna()),
            marathon_df['heartRate'] / marathon_df['AvgSpeed'],
            np.nan
        )
    else:
        # Nếu không có heartRate hợp lệ, không thêm các cột liên quan
        if 'heartRate' in marathon_df.columns:
            marathon_df['heartRate'] = np.nan

    return marathon_df.copy()

def analyze_per_user(df):
    """
    Phân tích dữ liệu theo từng người dùng, chỉ tính toán heartRate nếu có giá trị hợp lệ.

    Args:
        df (pd.DataFrame): DataFrame chứa dữ liệu marathon.

    Returns:
        tuple: (DataFrame đã phân tích, thống kê người dùng).
    """
    user_stats = {}
    user_id_col = 'UserId' if 'UserId' in df.columns else 'Id'
    user_ids = df[user_id_col].unique()

    for user_id in user_ids:
        user_data = df[df[user_id_col] == user_id]
        if len(user_data) >= 2:  # Chỉ phân tích người dùng có ít nhất 2 bản ghi
            user_stats[user_id] = {}
            # Tính thống kê cho các cột có sẵn
            for col in ['TotalSteps', 'TotalDistance', 'AvgSpeed', 'DistancePerStep']:
                if col in user_data.columns:
                    user_stats[user_id][f'avg_{col.lower()}'] = user_data[col].mean()
                    user_stats[user_id][f'std_{col.lower()}'] = user_data[col].std()
            # Chỉ tính thống kê heartRate nếu có giá trị hợp lệ
            if 'heartRate' in user_data.columns and user_data['heartRate'].notna().any():
                valid_heart_rates = user_data['heartRate'][user_data['heartRate'].notna()]
                user_stats[user_id]['avg_heartrate'] = valid_heart_rates.mean()
                user_stats[user_id]['std_heartrate'] = valid_heart_rates.std()
            user_stats[user_id]['record_count'] = len(user_data)

            # Tính tương quan giữa TotalSteps và các đặc trưng khác
            if len(user_data) >= 5:
                correlations = user_data[['TotalSteps', 'TotalDistance', 'AvgSpeed', 'TimeTaken']].corr()['TotalSteps'].drop('TotalSteps')
                user_stats[user_id]['step_correlations'] = correlations.to_dict()
                if 'heartRate' in user_data.columns and user_data['heartRate'].notna().any():
                    valid_data = user_data[['TotalSteps', 'heartRate']].dropna()
                    if len(valid_data) >= 2:
                        heart_rate_corr = valid_data.corr().iloc[0, 1]
                        user_stats[user_id]['step_heart_rate_correlation'] = heart_rate_corr

    # Xác định mức độ bất thường của từng bản ghi
    for col, prefix in zip(
            ['TotalSteps', 'AvgSpeed', 'DistancePerStep', 'heartRate'],
            ['StepDeviation', 'SpeedDeviation', 'DistPerStepDeviation', 'HeartRateDeviation']
    ):
        if col in df.columns and (col != 'heartRate' or df['heartRate'].notna().any()):
            df[prefix] = np.nan
            for user_id, stats in user_stats.items():
                user_indices = df[df[user_id_col] == user_id].index
                if len(user_indices) > 0 and f'avg_{col.lower()}' in stats and f'std_{col.lower()}' in stats:
                    valid_rows = user_indices if col != 'heartRate' else user_indices[df.loc[user_indices, 'heartRate'].notna()]
                    df.loc[valid_rows, prefix] = (
                                                         df.loc[valid_rows, col] - stats[f'avg_{col.lower()}']
                                                 ) / (stats[f'std_{col.lower()}'] + 1e-6)

    return df, user_stats

def extract_features(df):
    """
    Trích xuất đặc trưng, chỉ sử dụng heartRate nếu có giá trị hợp lệ.

    Args:
        df (pd.DataFrame): DataFrame chứa dữ liệu marathon.

    Returns:
        tuple: (Dữ liệu đặc trưng đã chuẩn hóa, DataFrame đặc trưng gốc).
    """
    available_basic_features = [col for col in [
        'TotalSteps', 'TotalDistance', 'VeryActiveDistance',
        'VeryActiveMinutes', 'AvgSpeed', 'DistancePerStep'
    ] if col in df.columns]

    # Chỉ thêm các đặc trưng heartRate nếu có giá trị hợp lệ
    heart_rate_features = []
    if 'heartRate' in df.columns and df['heartRate'].notna().any():
        heart_rate_features = [col for col in ['heartRate', 'HeartRatePerStep', 'HeartRatePerSpeed'] if col in df.columns]

    available_advanced_features = [col for col in ['Weekend', 'DayOfWeek'] if col in df.columns]
    available_user_features = [col for col in [
        'StepDeviation', 'SpeedDeviation', 'DistPerStepDeviation'
    ] if col in df.columns]
    if 'heartRate' in df.columns and df['heartRate'].notna().any():
        if 'HeartRateDeviation' in df.columns:
            available_user_features.append('HeartRateDeviation')

    all_available_features = available_basic_features + heart_rate_features + available_advanced_features + available_user_features

    if not all_available_features:
        raise ValueError("Không có đặc trưng nào khả dụng để phân tích")

    features = df[all_available_features].copy()
    features = features.fillna(0)  # Chỉ điền 0 cho các cột không phải heartRate

    scaler = StandardScaler()
    features_scaled = scaler.fit_transform(features)

    return features_scaled, features

def detect_anomalies_isolation_forest(features_scaled, contamination=0.05):
    model = IsolationForest(contamination=contamination, random_state=42, n_estimators=100)
    predictions = model.fit_predict(features_scaled)
    fraud_predictions = np.where(predictions == -1, 1, 0)
    return fraud_predictions, model

def detect_anomalies_lof(features_scaled, contamination=0.05):
    n_samples = features_scaled.shape[0]
    n_neighbors = min(20, n_samples - 1)
    lof = LocalOutlierFactor(n_neighbors=n_neighbors, contamination=contamination)
    predictions = lof.fit_predict(features_scaled)
    fraud_predictions = np.where(predictions == -1, 1, 0)
    lof_scores = -lof.negative_outlier_factor_
    return fraud_predictions, lof_scores

def detailed_fraud_analysis(df):
    """
    Phân tích chi tiết các trường hợp gian lận, bỏ qua heartRate nếu không có.

    Args:
        df (pd.DataFrame): DataFrame chứa dữ liệu marathon với cột IsFraud.

    Returns:
        pd.DataFrame: DataFrame với cột FraudType được cập nhật.
    """
    fraud_cases = df[df['IsFraud'] == 1]
    print(f"\n=== Phân tích chi tiết {len(fraud_cases)} trường hợp gian lận ===")

    if len(fraud_cases) == 0:
        print("Không có trường hợp gian lận được phát hiện.")
        return df

    df['FraudType'] = "Unknown"

    vehicle_fraud_mask = pd.Series(False, index=df.index)
    if 'AvgSpeed' in df.columns:
        vehicle_fraud_mask = (df['AvgSpeed'] > 12) & (df['IsFraud'] == 1)
        df.loc[vehicle_fraud_mask, 'FraudType'] = "Sử dụng phương tiện"

    shortcut_fraud_mask = pd.Series(False, index=df.index)
    if all(x in df.columns for x in ['SpeedDeviation', 'DistPerStepDeviation']):
        shortcut_fraud_mask = (df['SpeedDeviation'] > 2) & (df['DistPerStepDeviation'] > 2) & (df['IsFraud'] == 1)
        df.loc[shortcut_fraud_mask & ~vehicle_fraud_mask, 'FraudType'] = "Đi tắt đường"

    step_fraud_mask = pd.Series(False, index=df.index)
    if 'DistancePerStep' in df.columns:
        step_fraud_mask = (df['DistancePerStep'] > 0.001) & (df['IsFraud'] == 1) & (~vehicle_fraud_mask)
        if 'DistPerStepDeviation' in df.columns:
            step_fraud_mask = step_fraud_mask & (df['DistPerStepDeviation'] > 2)
        df.loc[step_fraud_mask & ~shortcut_fraud_mask & ~vehicle_fraud_mask, 'FraudType'] = "Khai báo sai số bước"

    heart_rate_fraud_mask = pd.Series(False, index=df.index)
    if 'heartRate' in df.columns and df['heartRate'].notna().any():
        heart_rate_fraud_mask = (df['heartRate'] < 60) & (df['TotalSteps'] > 10000) & (df['IsFraud'] == 1)
        df.loc[heart_rate_fraud_mask & ~step_fraud_mask & ~shortcut_fraud_mask & ~vehicle_fraud_mask, 'FraudType'] = "Nhịp tim bất thường"

    correlation_fraud_mask = pd.Series(False, index=df.index)
    if all(x in df.columns for x in ['TotalSteps', 'TotalDistance']):
        correlations = df[['TotalSteps', 'TotalDistance']].corr().iloc[0, 1]
        if correlations < 0.5:
            correlation_fraud_mask = (df['IsFraud'] == 1) & (~heart_rate_fraud_mask) & (~step_fraud_mask) & (~shortcut_fraud_mask) & (~vehicle_fraud_mask)
            df.loc[correlation_fraud_mask, 'FraudType'] = "Tương quan bất thường"

    unknown_fraud_mask = (df['IsFraud'] == 1) & (df['FraudType'] == "Unknown")
    df.loc[unknown_fraud_mask, 'FraudType'] = "Dữ liệu bất thường"

    fraud_type_counts = df[df['IsFraud'] == 1]['FraudType'].value_counts()
    print("\nPhân loại các trường hợp gian lận:")
    for fraud_type, count in fraud_type_counts.items():
        print(f"- {fraud_type}: {count} trường hợp")

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
        print("- Thông số trung bình trong các hoạt động gian lận:")
        for col, format_str in [
            ('AvgSpeed', "  + Tốc độ: {:.2f} km/h"),
            ('DistancePerStep', "  + Khoảng cách/bước: {:.5f} km"),
            ('TotalSteps', "  + Số bước: {:.0f}"),
            ('TotalDistance', "  + Quãng đường: {:.2f} km"),
            ('heartRate', "  + Nhịp tim: {:.1f} bpm")
        ]:
            if col in user_fraud.columns and (col != 'heartRate' or user_fraud['heartRate'].notna().any()):
                print(format_str.format(user_fraud[col][user_fraud[col].notna()].mean()))

    return df

def generate_final_report(df, fraud_cases):
    """
    Tạo báo cáo cuối cùng về gian lận.

    Args:
        df (pd.DataFrame): DataFrame chứa dữ liệu marathon.
        fraud_cases (pd.DataFrame): DataFrame chứa các trường hợp gian lận.

    Returns:
        dict: Báo cáo rủi ro theo người dùng.
    """
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

    if fraud_records > 0:
        print("\nThống kê theo loại gian lận:")
        fraud_types = fraud_cases['FraudType'].value_counts()
        for fraud_type, count in fraud_types.items():
            print(f"- {fraud_type}: {count} trường hợp ({count/fraud_records*100:.2f}%)")

    user_risk_scores = {}
    for user_id in df[user_id_col].unique():
        user_data = df[df[user_id_col] == user_id]
        fraud_data = user_data[user_data['IsFraud'] == 1]
        fraud_ratio = len(fraud_data) / len(user_data) if len(user_data) > 0 else 0

        fraud_weights = {
            "Sử dụng phương tiện": 1.0,
            "Đi tắt đường": 0.8,
            "Khai báo sai số bước": 0.6,
            "Nhịp tim bất thường": 0.7,
            "Tương quan bất thường": 0.5,
            "Dữ liệu bất thường": 0.4
        }

        risk_score = fraud_ratio * 100
        if len(fraud_data) > 0:
            fraud_type_counts = fraud_data['FraudType'].value_counts()
            if len(fraud_type_counts) > 0:
                worst_fraud_type = fraud_type_counts.index[0]
                worst_fraud_weight = fraud_weights.get(worst_fraud_type, 0.5)
                risk_score = risk_score * (1 + worst_fraud_weight)
        risk_score = min(100, max(0, risk_score))

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

    print("\nBáo cáo rủi ro theo người dùng:")
    for user_id, risk_data in user_risk_scores.items():
        if risk_data['fraud_count'] > 0:
            print(f"- Người dùng {user_id}:")
            print(f"  + Điểm rủi ro: {risk_data['risk_score']:.1f}/100 (Mức: {risk_data['risk_level']})")
            print(f"  + Số hoạt động gian lận: {risk_data['fraud_count']}/{risk_data['total_activities']} ({risk_data['fraud_ratio']*100:.1f}%)")

    return user_risk_scores