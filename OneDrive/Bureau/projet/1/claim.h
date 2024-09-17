#ifndef CLAIM_H
#define CLAIM_H

#include <QWidget>

namespace Ui {
class Claim;
}

class Claim : public QWidget
{
    Q_OBJECT

public:
    explicit Claim(QWidget *parent = nullptr);
    ~Claim();

private:
    Ui::Claim *ui;
};

#endif // CLAIM_H
