#ifndef FOURNISSEUR_H
#define FOURNISSEUR_H
#include "qstring.h"
#include "QtSql/QSqlQueryModel"
#include "QtSql/QSqlQuery"


class Fournisseur
{
private:
    int id_f, numero_de_telephone_f,code_postal;
    QString nom_de_la_societe, adresse_email_f, adresse_du_siege_social, pays, ville;

public:
    Fournisseur();
    Fournisseur(int,QString,int,QString,QString,QString,QString,int);

    int get_id_f();
    void set_id_f(int);
    int get_numero_de_telephone_f();
    void set_numero_de_telephone_f(int);
    int get_code_postal();
    void set_code_postal(int);
    QString get_nom_de_la_societe();
    void set_nom_de_la_societe(QString);
    QString get_adresse_email_f();
    void set_adresse_email_f(QString);
    QString get_adresse_du_siege_social();
    void set_adresse_du_siege_social(QString);
    QString get_ville();
    void set_ville(QString);
    QString get_pays();
    void set_pays(QString);
    QSqlQueryModel * afficher();
    bool ajouter();
    bool supprimer(int);
    bool modifier(int);
    QSqlQueryModel * recherche(QString ar);


};

#endif // FOURNISSEUR_H
