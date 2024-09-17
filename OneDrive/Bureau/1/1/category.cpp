#include "category.h"

Category::Category(const QString& name, const QStringList& subcategories)
    : name(name), subcategories(subcategories) {}

QString Category::getName() const {
    return name;
}

QStringList Category::getSubcategories() const {
    return subcategories;
}
