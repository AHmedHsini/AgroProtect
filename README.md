# 🌱 AgroProtect — Smart Agricultural Scoring & Decision System

AgroProtect is an intelligent agricultural decision-support platform designed to evaluate farm viability, reduce investment risk, and recommend optimal crop strategies using data-driven analysis and machine learning.

---

## 🎯 Project Overview

Agriculture in Tunisia faces major challenges:
- Climate variability
- Soil degradation
- Financial uncertainty
- Lack of objective evaluation tools

AgroProtect addresses these issues by transforming complex agricultural data into:
- 📊 A reliable agricultural score
- 🌾 Smart crop recommendations
- 💰 Investment decisions (FINANCER / SURVEILLER / REFUSER)

---

## 🧠 Key Features

### 🔹 1. Agricultural Scoring Engine
Multi-dimensional scoring based on:
- 🌱 Agronomic factors (soil quality, pH, organic matter)
- 🌦️ Climate data (rainfall)
- 🛰️ Productivity (NDVI satellite data)
- 📈 Stability (historical performance)
- 📊 Market analysis (FAO data)

---

### 🔹 2. Machine Learning Integration
- Model: Linear Regression (Scikit-learn)
- Predicts agricultural viability score
- Continuous learning via Data Feedback Loop

---

### 🔹 3. Market-Oriented Intelligence 🇹🇳
Focus on Tunisian food security:
- Import dependency analysis
- FAO data integration
- Strategic crop prioritization

**Formula:**
ImportDependencyRatio = Import / (Production + Import)


---

### 🔹 4. Smart Crop Recommendation
Recommends optimal crops based on:
- Soil compatibility
- Climate conditions
- Market demand

---

### 🔹 5. Decision Support System
Final output:
- ✅ FINANCER
- ⚠️ SURVEILLER
- ❌ REFUSER

---

## 🏗️ System Architecture
            +----------------------+
            |   External APIs      |
            | Soil | Weather | FAO |
            +----------+-----------+
                       |
                       v
            +----------------------+
            |   Spring Boot API    |
            |  (Scoring Engine)    |
            +----------+-----------+
                       |
                       v
            +----------------------+
            |  ML Microservice     |
            |  (Flask + Python)    |
            +----------------------+

            
## 🧩 Project Structure


AgroProtect/
│
├── controllers/ # REST APIs
├── services/ # Business logic (Scoring, Decision)
├── repositories/ # Database access
├── entities/ # JPA models
├── dto/ # Data Transfer Objects
│
├── ml-service/ # Python ML microservice
│ ├── app.py
│ ├── model.pkl
│ └── data.csv
│
└── config/ # Configuration files

---

## ⚙️ Technologies Used

### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- REST API

### Machine Learning
- Python
- Flask
- Scikit-learn
- Pandas

### Data Sources
- FAO (Food and Agriculture Organization)
- Satellite NDVI APIs
- Soil APIs (ISRIC)

---

## 📊 Scoring Formula
FinalScore =
0.25 * Agronomic +
0.20 * Climate +
0.20 * Productivity +
0.15 * Stability +
0.20 * Market

---

## 🚀 API Endpoints (Examples)

| Method | Endpoint | Description |
|--------|---------|------------|
| GET | /api/scoring/{terrainId} | Calculate score |
| GET | /api/scoring/breakdown | Detailed scores |
| GET | /api/decision/{terrainId} | Final decision |
| GET | /api/recommendation/{terrainId} | Crop recommendation |

---

## 🔁 Machine Learning Flow

1. Data collected from scoring
2. Sent to ML microservice
3. Prediction returned
4. Data stored for retraining
5. Model updated continuously

---

## 🧪 Testing

- Postman for API testing
- Unit tests for services
- Mock APIs for external integrations

---

## 🔐 Robustness

- API fallback mechanisms
- Error handling (try/catch)
- ML fallback to rule-based scoring

---

## 📈 Future Improvements

- Deep Learning models
- Real-time data integration
- Advanced dashboards
- Asynchronous microservices architecture

---

## 🤝 Contribution

This project was developed as part of an academic research initiative focused on agricultural innovation and financial inclusion.

---

## 📌 Author

- Your Name

---

## 📄 License

This project is for academic and research purposes.
