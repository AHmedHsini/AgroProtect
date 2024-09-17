#ifndef CATEGORY_H
#define CATEGORY_H

#include <QStringList>
#include <QMap>

class Category {
public:
    Category(const QString& name, const QStringList& subcategories);

    QString getName() const;
    QStringList getSubcategories() const;


private:
    QString name;
    QStringList subcategories;
};

#endif // CATEGORY_H

