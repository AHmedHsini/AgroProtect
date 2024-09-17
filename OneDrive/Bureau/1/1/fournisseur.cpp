#include "fournisseur.h"

Fournisseur::Fournisseur()
{
    id_f =0; numero_de_telephone_f=0; code_postal=0; nom_de_la_societe=" ";
    adresse_email_f=" "; adresse_du_siege_social= " "; ville =" "; pays= " ";
}
Fournisseur::Fournisseur(int id_f,QString nom_de_la_societe,int numero_de_telephone_f,QString adresse_email_f,QString adresse_du_siege_social,QString pays,QString ville,int code_postal){
        this->id_f = id_f; this->nom_de_la_societe = nom_de_la_societe; this->numero_de_telephone_f = numero_de_telephone_f;
    this->adresse_email_f=adresse_email_f; this->adresse_du_siege_social=adresse_du_siege_social;this->pays=pays;this->ville=ville;this->code_postal=code_postal;

}

int Fournisseur::get_id_f(){return id_f;}
void Fournisseur::set_id_f(int id_f){this->id_f=id_f;}
int Fournisseur::get_numero_de_telephone_f(){return numero_de_telephone_f;}
void Fournisseur::set_numero_de_telephone_f(int numero_de_telephone_f){this->numero_de_telephone_f = numero_de_telephone_f;}
int Fournisseur::get_code_postal(){return  code_postal;}
void Fournisseur::set_code_postal(int code_postal){this->code_postal = code_postal;}
QString Fournisseur::get_nom_de_la_societe(){return nom_de_la_societe;}
void Fournisseur::set_nom_de_la_societe(QString nom_de_la_societe){this->nom_de_la_societe = nom_de_la_societe;}
QString Fournisseur::get_adresse_email_f(){return adresse_email_f;}
void Fournisseur::set_adresse_email_f(QString adresse_email_f){this->adresse_email_f=adresse_email_f;}
QString Fournisseur::get_adresse_du_siege_social(){return adresse_du_siege_social;}
void Fournisseur::set_adresse_du_siege_social(QString adresse_du_siege_social){this->adresse_du_siege_social = adresse_du_siege_social;}
QString Fournisseur::get_ville(){return ville;}
void Fournisseur::set_ville(QString ville){this->ville = ville;}
QString Fournisseur::get_pays(){return pays;}
void Fournisseur::set_pays(QString pays){this->pays = pays;}

bool Fournisseur::ajouter(){
    QSqlQuery query;
    QString id_string = QString::number(id_f);//convertion du int vers QString
    QString num_string = QString::number(numero_de_telephone_f);
    QString code_string = QString::number(code_postal);
          query.prepare("INSERT INTO FOURNISSEUR (ID_F, NOM_DE_LA_SOCIETE, NUMERO_DE_TELEPHONE_F, ADRESSE_EMAIL_F, ADRESSE_DU_SIEGE_SOCIAL, PAYS, VILLE, CODE_POSTAL) " "VALUES (:ID_F, :NOM_DE_LA_SOCIETE, :NUMERO_DE_TELEPHONE_F, :ADRESSE_EMAIL_F, :ADRESSE_DU_SIEGE_SOCIAL, :PAYS, :VILLE, :CODE_POSTAL)");
          query.bindValue(":ID_F", id_string);
          query.bindValue(":NOM_DE_LA_SOCIETE", nom_de_la_societe);
          query.bindValue(":NUMERO_DE_TELEPHONE_F", num_string);
          query.bindValue(":ADRESSE_EMAIL_F", adresse_email_f);
          query.bindValue(":ADRESSE_DU_SIEGE_SOCIAL", adresse_du_siege_social);
          query.bindValue(":PAYS", pays);
          query.bindValue(":VILLE", ville);
          query.bindValue(":CODE_POSTAL", code_string);
          return query.exec();
}

QSqlQueryModel * Fournisseur::afficher()
{
    QSqlQueryModel * model = new QSqlQueryModel();
    model->setQuery("Select * from FOURNISSEUR");
    model->setHeaderData(0,Qt::Horizontal,QObject::tr("ID"));
    model->setHeaderData(1,Qt::Horizontal,QObject::tr("NOM S"));
    model->setHeaderData(2,Qt::Horizontal,QObject::tr("N°TEL"));
    model->setHeaderData(3,Qt::Horizontal,QObject::tr("EMAIL"));
    model->setHeaderData(4,Qt::Horizontal,QObject::tr("ADRESSE S"));
    model->setHeaderData(5,Qt::Horizontal,QObject::tr("PAYS"));
    model->setHeaderData(6,Qt::Horizontal,QObject::tr("VILLE"));
    model->setHeaderData(7,Qt::Horizontal,QObject::tr("CODE POSTAL"));
    return model;
}

bool Fournisseur::supprimer(int id){
    QSqlQuery query;
    QString id_string = QString::number(id);
    query.prepare("Delete from FOURNISSEUR where ID_F = :ID_F");
    query.bindValue(":ID_F",id_string);
    return  query.exec();
}

bool Fournisseur::modifier(int id_f){
    QSqlQuery query;
    QString id_string = QString::number(id_f);
    QString num_string = QString::number(numero_de_telephone_f);
    QString code_string = QString::number(code_postal);
    query.prepare("UPDATE FOURNISSEUR SET NOM_DE_LA_SOCIETE= :NOM_DE_LA_SOCIETE, NUMERO_DE_TELEPHONE_F= :NUMERO_DE_TELEPHONE_F, ADRESSE_EMAIL_F= :ADRESSE_EMAIL_F, ADRESSE_DU_SIEGE_SOCIAL= :ADRESSE_DU_SIEGE_SOCIAL, PAYS = :PAYS, VILLE = :VILLE, CODE_POSTAL = :CODE_POSTAL "
                        " WHERE  ID_F = '"+id_string+"' ");
    query.bindValue(":ID_F", id_string);
    query.bindValue(":NOM_DE_LA_SOCIETE", nom_de_la_societe);
    query.bindValue(":NUMERO_DE_TELEPHONE_F", num_string);
    query.bindValue(":ADRESSE_EMAIL_F", adresse_email_f);
    query.bindValue(":ADRESSE_DU_SIEGE_SOCIAL", adresse_du_siege_social);
    query.bindValue(":PAYS", pays);
    query.bindValue(":VILLE", ville);
    query.bindValue(":CODE_POSTAL", code_string);
    return query.exec();
}

QSqlQueryModel * Fournisseur::recherche(QString ar)
{
    QSqlQueryModel * model = new QSqlQueryModel();
    model->setQuery("Select * from FOURNISSEUR where ID_F like '%"+ar+"%' or NOM_DE_LA_SOCIETE like '%"+ar+"%' or NUMERO_DE_TELEPHONE_F like '%"+ar+"%' or ADRESSE_EMAIL_F like '%"+ar+"%' or ADRESSE_DU_SIEGE_SOCIAL like '%"+ar+"%' or PAYS like '%"+ar+"%' or VILLE like '%"+ar+"%' or CODE_POSTAL like '%"+ar+"%' ");
    return model;
}
