from flask import Flask, request, jsonify
import joblib
import numpy as np

# 🚀 Initialisation Flask
app = Flask(__name__)

# 📥 Charger le modèle entraîné
model = joblib.load("model.pkl")

# 🎯 Endpoint prédiction IA
@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.json

        # 🔍 récupérer les features
        funding_goal = data.get("funding_goal", 0)
        collected_amount = data.get("collected_amount", 0)
        avg_revenue = data.get("avg_revenue", 0)
        expenses = data.get("expenses", 0)
        duration_months = data.get("duration_months", 12)

        # 📊 transformer en tableau
        features = np.array([[
            funding_goal,
            collected_amount,
            avg_revenue,
            expenses,
            duration_months
        ]])


        # 🤖 prédiction
        prediction = model.predict(features)[0]
        probability = model.predict_proba(features)[0][1]

        feature_names = [
            "funding_goal",
            "collected_amount",
            "avg_revenue",
            "expenses",
            "duration_months"
        ]

        importances = model.feature_importances_

        return jsonify({
            "success": int(prediction),
            "probability": float(probability),
            "explanation": dict(zip(feature_names, importances.tolist()))
        })

    except Exception as e:
        return jsonify({
            "error": str(e)
        }), 500


# 🔥 Lancement serveur
if __name__ == "__main__":
    print("🚀 ML API running on http://localhost:8001")
    app.run(port=8001)