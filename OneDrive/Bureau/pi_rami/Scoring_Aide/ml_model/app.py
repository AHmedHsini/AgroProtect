from flask import Flask, request, jsonify
import pandas as pd
from sklearn.linear_model import LinearRegression

app = Flask(__name__)

# dataset
data = pd.read_csv("data.csv")

X = data[["agronomique", "climatique", "productivite", "stabilite", "market"]]
y = data["scoreFinal"]

model = LinearRegression()
model.fit(X, y)

@app.route("/health", methods=["GET"])
def health():
    return {"status": "OK"}

@app.route("/predict", methods=["POST"])
def predict():
    data = request.json

    features = [[
        data["agronomique"],
        data["climatique"],
        data["productivite"],
        data["stabilite"],
        data["market"]
    ]]

    prediction = model.predict(features)

    return jsonify({"predictedScore": float(prediction[0])})


@app.route("/add-data", methods=["POST"])
def add_data():
    try:
        new_data = request.json
        df = pd.read_csv("data.csv")
        df = pd.concat([df, pd.DataFrame([new_data])])
        df.to_csv("data.csv", index=False)
        return jsonify({"message": "Data added successfully"})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(port=5000)
