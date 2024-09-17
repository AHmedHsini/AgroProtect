#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QMessageBox>
#include <QRegExpValidator>
#include <QRegularExpression>
#include <QPrinter>
#include <QPrintDialog>
#include "fournisseur.h"
#include "stats.h"

QT_BEGIN_NAMESPACE
namespace Ui { class MainWindow; }
QT_END_NAMESPACE

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();

private slots:
    void on_pushButton_clicked();

    void on_tableView_clicked(const QModelIndex &index);

    void on_pushButton_3_clicked();

    void on_pushButton_5_clicked();

    void on_lineEdit_textChanged(const QString &arg1);

    void on_comboBox_currentIndexChanged(int index);

    void on_pushButton_6_clicked();

    void on_pushButton_7_clicked();

    void on_pushButton_pdf_clicked();

private:
    Ui::MainWindow *ui;
    Fournisseur FTMP;
    stats *s;

};
#endif // MAINWINDOW_H
