#include "claim.h"
#include "ui_claim.h"

Claim::Claim(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::Claim)
{
    ui->setupUi(this);
}

Claim::~Claim()
{
    delete ui;
}
