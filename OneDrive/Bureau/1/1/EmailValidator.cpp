#include "EmailValidator.h"
#include <QRegExp>

EmailValidator::EmailValidator(QObject* parent) : QValidator(parent) {}

QValidator::State EmailValidator::validate(QString& input, int& pos) const
{
    QRegExp emailRegex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$");
    if (emailRegex.exactMatch(input)) {
        return QValidator::Acceptable;
    } else {
        return QValidator::Invalid;
    }
}
