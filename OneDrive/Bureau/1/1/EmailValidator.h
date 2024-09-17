#ifndef EMAILVALIDATOR_H
#define EMAILVALIDATOR_H

#include <QValidator>

class EmailValidator : public QValidator
{
public:
    EmailValidator(QObject* parent = nullptr);

    State validate(QString& input, int& pos) const override;
};

#endif // EMAILVALIDATOR_H
