#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "EmailValidator.h"

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    ui->lineEdit_id->setValidator(new QIntValidator (0,99999999,this));
    ui->lineEdit_num->setValidator(new QIntValidator (0,99999999,this));
    ui->lineEdit_code->setValidator(new QIntValidator (0,99999999,this));
    ui->lineEdit_id_2->setValidator(new QIntValidator (0,99999999,this));
    ui->lineEdit_num_2->setValidator(new QIntValidator (0,99999999,this));
    ui->lineEdit_code_2->setValidator(new QIntValidator (0,99999999,this));

    ui->tableView->setModel(FTMP.afficher());
}

MainWindow::~MainWindow()
{
    delete ui;
}


void MainWindow::on_pushButton_clicked()//BOUTON AJOUTER
{
    int id_f = ui->lineEdit_id->text().toInt();
    QString nom_de_la_societe = ui->lineEdit_nomS->text();
    int numero_de_telephone_f = ui->lineEdit_num->text().toInt();
    QString adresse_email_f = ui->lineEdit_email->text();
    QString adresse_de_siege_social = ui->lineEdit_adresse->text();
    QString pays = ui->comboBox_pays->currentText();
    QString ville = ui->lineEdit_ville->text();
    int code_postal = ui->lineEdit_code->text().toInt();

    if (numero_de_telephone_f < 10000000 || numero_de_telephone_f > 99999999) {
        QMessageBox::critical(nullptr, QObject::tr("Erreur"),
            QObject::tr("Numéro de téléphone invalide (8 chiffres requis)."), QMessageBox::Cancel);
        return;
    }

    QRegularExpression emailRegex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$");
    QRegularExpressionMatch match = emailRegex.match(adresse_email_f);
    if (!match.hasMatch()) {
        QMessageBox::critical(nullptr, QObject::tr("Erreur"),
            QObject::tr("Adresse e-mail invalide (format attendu : xx@xx.xx)."), QMessageBox::Cancel);
        return;
    }

    Fournisseur F(id_f, nom_de_la_societe, numero_de_telephone_f, adresse_email_f, adresse_de_siege_social, pays, ville, code_postal);
    bool test = F.ajouter();
    if (test)
    {
        QMessageBox::information(nullptr,QObject::tr("OK"),
                                 QObject::tr("Ajout effectué \n" "Click cancel to exit."),QMessageBox::Cancel);
    }
    else
        QMessageBox::critical(nullptr,QObject::tr("NOT OK"),
                                 QObject::tr("Ajout non effectué \n" "Click cancel to exit."),QMessageBox::Cancel);
    ui->tableView->setModel(FTMP.afficher());//AFFICHAGE AUTO
    ui->tabWidget->setCurrentIndex(1);
}


void MainWindow::on_tableView_clicked(const QModelIndex &index)
{
    QString val=ui->tableView->model()->data(index).toString();
    QSqlQuery qry;
    qry.prepare("select * from FOURNISSEUR where ID_F=:val");
    qry.bindValue(":val",val);
    if (qry.exec())
    {
        while(qry.next())
        {
            ui->lineEdit_id_2->setText(qry.value(0).toString());
            ui->lineEdit_nomS_2->setText(qry.value(1).toString());
            ui->lineEdit_num_2->setText(qry.value(2).toString());
            ui->lineEdit_email_2->setText(qry.value(3).toString());
            ui->lineEdit_adresse_2->setText(qry.value(4).toString());
            ui->lineEdit_ville_2->setText(qry.value(6).toString());
            ui->lineEdit_code_2->setText(qry.value(7).toString());
            QString infos = (" Id : \t" + qry.value(0).toString() + "\n Nom de la societe : \t" + qry.value(1).toString() +"\n N°Tel : \t"+ qry.value(2).toString() + "\n Adresse email : \t" +qry.value(3).toString() + "\n Adresse du sciége social : \t"+ qry.value(4).toString() + "\n Ville : \t"+ qry.value(6).toString() + "\n Code Postal : \t"+ qry.value(7).toString());
            ui->pdf->setText(infos);
        }
    }
        else
        {
           QMessageBox::warning(this,"error","no information");
        }

}

void MainWindow::on_pushButton_3_clicked()//BOUTONS SUPPRIMER
{
    int id = ui->lineEdit_id_2->text().toInt();
    bool test=FTMP.supprimer(id);
    if (test)
    {
        QMessageBox::information(nullptr,QObject::tr("OK"),
                                 QObject::tr("Suppression effectué \n" "Click cancel to exit."),QMessageBox::Cancel);
    }
    else
        QMessageBox::critical(nullptr,QObject::tr("NOT OK"),
                                 QObject::tr("Suppression non effectué \n" "Click cancel to exit."),QMessageBox::Cancel);

    ui->tableView->setModel(FTMP.afficher());//AFFICHAGE AUTO
    ui->tabWidget->setCurrentIndex(1);
}

void MainWindow::on_pushButton_5_clicked()
{
    int id_f = ui->lineEdit_id_2->text().toInt();
    QString nom_de_la_societe = ui->lineEdit_nomS_2->text();
    int numero_de_telephone_f = ui->lineEdit_num_2->text().toInt();
    QString adresse_email_f = ui->lineEdit_email_2->text();
    QString adresse_de_siege_social = ui->lineEdit_adresse_2->text();
    QString pays = ui->comboBox_pays_2->currentText();
    QString ville = ui->lineEdit_ville_2->text();
    int code_postal = ui->lineEdit_code_2->text().toInt();


    if (numero_de_telephone_f < 10000000 || numero_de_telephone_f > 99999999) {
        QMessageBox::critical(nullptr, QObject::tr("Erreur"),
            QObject::tr("Numéro de téléphone invalide (8 chiffres requis)."), QMessageBox::Cancel);
        return;
    }

    QRegularExpression emailRegex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$");//les critéres d'un email (forme : xx@xx.tn)
    QRegularExpressionMatch match = emailRegex.match(adresse_email_f);// match : comparaison entre emailRegex et adresse_email_f
    if (!match.hasMatch()) {//hasMatch = egale
        QMessageBox::critical(nullptr, QObject::tr("Erreur"),
            QObject::tr("Adresse e-mail invalide (format attendu : xx@xx.xx)."), QMessageBox::Cancel);
        return;
    }

    Fournisseur F(id_f, nom_de_la_societe, numero_de_telephone_f, adresse_email_f, adresse_de_siege_social, pays, ville, code_postal);
    bool test = F.modifier(id_f);
    if (test)
    {
        QMessageBox::information(nullptr,QObject::tr("OK"),
                                 QObject::tr("Modification effectué \n" "Click cancel to exit."),QMessageBox::Cancel);
    }
    else
        QMessageBox::critical(nullptr,QObject::tr("NOT OK"),
                                 QObject::tr("Modification non effectué \n" "Click cancel to exit."),QMessageBox::Cancel);

    ui->tableView->setModel(FTMP.afficher());//AFFICHAGE AUTO
    ui->tabWidget->setCurrentIndex(1);
}

void MainWindow::on_lineEdit_textChanged(const QString &arg1)
{
    ui->tableView->setModel(FTMP.recherche(arg1));
}

void MainWindow::on_comboBox_currentIndexChanged(int index)
{
    QSqlQueryModel * model = new QSqlQueryModel();
    switch (index)
    {
    case 1:
        model->setQuery("Select * from FOURNISSEUR ORDER BY ID_F ASC");
        ui->tableView->setModel(model);
        break;
    case 2:
        model->setQuery("Select * from FOURNISSEUR ORDER BY ID_F DESC");
        ui->tableView->setModel(model);
        break;
    case 3:
        model->setQuery("Select * from FOURNISSEUR ORDER BY NOM_DE_LA_SOCIETE ASC");
        ui->tableView->setModel(model);
        break;
    case 4:
        model->setQuery("Select * from FOURNISSEUR ORDER BY NOM_DE_LA_SOCIETE DESC");
        ui->tableView->setModel(model);
        break;
    case 5:
        model->setQuery("Select * from FOURNISSEUR ORDER BY PAYS ASC");
        ui->tableView->setModel(model);
        break;
    case 6:
        model->setQuery("Select * from FOURNISSEUR ORDER BY PAYS DESC");
        ui->tableView->setModel(model);
        break;
    case 7:
        model->setQuery("Select * from FOURNISSEUR ORDER BY VILLE ASC");
        ui->tableView->setModel(model);
        break;
    case 8:
        model->setQuery("Select * from FOURNISSEUR ORDER BY VILLE DESC");
        ui->tableView->setModel(model);
        break;
    }
}

void MainWindow::on_pushButton_6_clicked()
{
    s = new stats ();
    s->setWindowTitle("lES PAYS AVEC LES PLUS DE FOURNISSEURS");
    s->pie();
    s->show();
}

void MainWindow::on_pushButton_7_clicked()
{
    ui->tabWidget->show();
    ui->tabWidget->setCurrentIndex(3);
}

void MainWindow::on_pushButton_pdf_clicked()
{
    QPrinter printer;
    printer.setPrinterName("FOURNISSEUR");
    QPrintDialog dialog(&printer,this);
    if (dialog.exec() == QDialog::Rejected) return;
    ui->pdf->print(&printer);
}
