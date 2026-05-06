import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression

# 📊 Charger dataset
data = pd.read_csv("data.csv")

# 🎯 Features (X) et target (y)
X = data[["agronomique", "climatique", "productivite", "stabilite", "market"]]
y = data["scoreFinal"]

# 🔀 Séparation train/test
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)

# 🤖 Modèle
model = LinearRegression()
model.fit(X_train, y_train)

# 📈 Prédiction exemple
prediction = model.predict([[90, 50, 68, 87, 54]])

print("Score prédit :", prediction[0])
