#ifndef STATS_H
#define STATS_H
#include <QSqlQuery>
#include <QApplication>
#include <QtCharts/QLegend>
#include <QtCharts>
#include <QtCharts/QPieSeries>
#include <QtCharts/QPieSlice>
#include <QtCharts/QChartView>
#include <QtCharts/QLineSeries>
#include <QDialog>

QT_CHARTS_USE_NAMESPACE

namespace Ui {
class stats;
}

class stats : public QDialog
{
    Q_OBJECT

public:
    explicit stats(QWidget *parent = nullptr);
    ~stats();
    void pie();

private slots:
    void on_pushButton_9_clicked();

private:
    Ui::stats *ui;
};

#endif // STATS_H
