#ifndef SMSING_H
#define SMSING_H

#include <QCoreApplication>
#include <QProcess>
#include <QDebug>
#include <QDir>
#include <QFile>
#include <QJsonDocument>
#include <QJsonObject>
#include <QMap>
#include <QStringList>
#include <QJsonArray>
#include <QDateTime>
#include <QResource>
#include <QByteArray>

class Smsing
{

private:
    QString folderPath, filePath, py_script_path;

    QMap<QString, QVariant> smsData;


public:
    Smsing();

    bool copyResourceToFile(const QString &fileName);
    QString executePythonScript(QString path);
    void createFolderIfNotExists(const QString &folderPath);
    void createJsonFile(const QString &filePath, const QMap<QString, QVariant> &dataMap);
    QMap<QString, QVariant> readJsonFile(const QString &filePath);
    void set_sms_atrb(QString atrb, QString txt);
    QString get_sms_atrb(QString atrb);
    bool sendSms(QString to, QString body);

};

#endif // SMSING_H
