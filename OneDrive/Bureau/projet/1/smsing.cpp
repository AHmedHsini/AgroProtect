#include "smsing.h"

Smsing::Smsing()
{
    folderPath = "";
    filePath = "";
    py_script_path = "";

    // Populate the QMap with email data
    smsData["type"] = "sms";
    smsData["sid"] = "AC247219681682b94b07e9ef6446fcbb0a";
    smsData["token"] = "e241dc4dfe2834a63aadf6347e3256c0";
    smsData["from"] = "+17272889005";
    smsData["to"] = "";
    smsData["body"] = "";
}

bool Smsing::copyResourceToFile(const QString &fileName) {

    // Path to the Python script in the resources
    QString scriptResourcePath = ":/scripts/" + fileName;

    QString scriptCopyPath = "./" + fileName;

    if (QFile::exists(scriptCopyPath)){
        qDebug() << "File already exists in the current directory.";
        return true;
    }

    // Copy the Python script from resources to the temporary directory
    QFile scriptCopy(scriptCopyPath);
        if (scriptCopy.open(QIODevice::WriteOnly | QIODevice::Truncate)) {
            QFile scriptResource(scriptResourcePath);
            if (scriptResource.open(QIODevice::ReadOnly)) {
                scriptCopy.write(scriptResource.readAll());
                scriptResource.close();
            }
            scriptCopy.close();

            qDebug() << "File copied to the current directory.";
            return true;
        } else {
            qDebug() << "Failed to copy the Python script.";
            return false;
        }

}

QString Smsing::executePythonScript(QString path)
{
    // Create a QProcess object
    QProcess pythonProcess;

    // Set the Python interpreter executable (e.g., "python" or "python3")
    pythonProcess.setProgram("python");

    // Define the arguments, including the path to your Python script
    QStringList arguments;
    arguments << path; // Replace with your script's path

    // Set the arguments for the Python script
    pythonProcess.setArguments(arguments);

    // Start the Python process
    pythonProcess.start();

    // Wait for the process to finish
    pythonProcess.waitForFinished();

    // Read the output of the Python script
    QByteArray output = pythonProcess.readAllStandardOutput();
    QString outputStr(output);

    // Handle the output as needed
    //qDebug() << "Python script output:\n" << outputStr;

    bool res = (outputStr == "SMS sent successfully!\r\n");
    if (res){
        return "ok";
    }
    else {
        return outputStr;
    }
}

void Smsing::createFolderIfNotExists(const QString &folderPath) {
    QDir dir(folderPath);

    // Check if the directory already exists
    if (!dir.exists()) {
        // Attempt to create the directory
        if (dir.mkpath(".")) {
            //qDebug() << "Directory created successfully: " << folderPath;
        } else {
            //qWarning() << "Failed to create directory: " << folderPath;
        }
    } else {
        //qDebug() << "Directory already exists: " << folderPath;
    }
}

void Smsing::createJsonFile(const QString &filePath, const QMap<QString, QVariant> &dataMap) {
    QFile file(filePath);

    if (file.open(QIODevice::WriteOnly | QIODevice::Text)) {
        QJsonObject jsonObject;

        // Convert the QMap to a QJsonObject
        QMapIterator<QString, QVariant> iter(dataMap);
        while (iter.hasNext()) {
            iter.next();

            // Check if the value is a QStringList
            if (iter.value().canConvert<QStringList>()) {
                QStringList stringList = iter.value().toStringList();
                QJsonArray jsonArray;
                for (const QString &str : stringList) {
                    jsonArray.append(str);
                }
                jsonObject[iter.key()] = jsonArray;
            } else {
                jsonObject[iter.key()] = QJsonValue::fromVariant(iter.value());
            }
        }

        QJsonDocument jsonDoc(jsonObject);
        QByteArray jsonData = jsonDoc.toJson();

        file.write(jsonData);
        file.close();

        qDebug() << "JSON file created successfully: " << filePath;
    } else {
        qWarning() << "Failed to create JSON file: " << filePath;
    }
}

QMap<QString, QVariant> Smsing::readJsonFile(const QString &filePath) {
    QMap<QString, QVariant> resultMap;

    QFile file(filePath);

    if (file.open(QIODevice::ReadOnly | QIODevice::Text)) {
        QByteArray jsonData = file.readAll();

        QJsonDocument jsonDoc = QJsonDocument::fromJson(jsonData);

        if (jsonDoc.isObject()) {
            QJsonObject jsonObject = jsonDoc.object();

            // Iterate through all keys and values in the JSON object
            QStringList keys = jsonObject.keys();
            for (const QString &key : keys) {
                QJsonValue jsonValue = jsonObject.value(key);

                if (jsonValue.isString()) {
                    resultMap[key] = jsonValue.toString();
                } else if (jsonValue.isArray()) {
                    QStringList stringList;
                    QJsonArray jsonArray = jsonValue.toArray();
                    for (const QJsonValue &arrayValue : jsonArray) {
                        if (arrayValue.isString()) {
                            stringList.append(arrayValue.toString());
                        }
                    }
                    resultMap[key] = stringList;
                } else {
                    // Handle other data types as needed
                }
            }
        } else {
            qWarning() << "JSON data is not an object in file: " << filePath;
        }

        file.close();
    } else {
        qWarning() << "Failed to open JSON file for reading: " << filePath;
    }

    return resultMap;
}

void Smsing::set_sms_atrb(QString atrb, QString txt){
    if (atrb == "to"){
        smsData["to"] = txt;
    }
    else if (atrb == "body"){
        smsData["body"] = txt;
    }
}

QString Smsing::get_sms_atrb(QString atrb){
    if (atrb == "to"){
        return smsData["to"].toString();
    }
    else if (atrb == "from"){
        return smsData["from"].toString();
    }
    else if (atrb == "body"){
        return smsData["body"].toString();
    }
    else{
        return "";
    }
}

bool Smsing::sendSms(QString to, QString body){

    bool file_exsits = copyResourceToFile("mail_for_qt.py");

    if (file_exsits){

        set_sms_atrb("to", to);
        set_sms_atrb("body", body);

        //QString folderPath = "D:/esprit study/2eme/QT/projects/try2";
        QString folderPath = ".";

        createFolderIfNotExists(folderPath);

        QString filePath = folderPath + "/jsonfile.json";

        // Call createJsonFile with the QMap
        createJsonFile(filePath, smsData);

        // Execute the Python script
        //QString py_script_path = "D:/esprit study/2eme/QT/projects/try2/mail_for_qt.py";
        QString py_script_path = "./mail_for_qt.py";
        QString res = executePythonScript(py_script_path);
        qDebug() << res;

        return (res == "ok");
    }
    return "";
}



