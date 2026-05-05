CREATE TABLE IF NOT EXISTS groupe_projet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_projet VARCHAR(255) NOT NULL,
    matiere VARCHAR(255),
    nbr_membre INT DEFAULT 1,
    statut VARCHAR(50) DEFAULT 'Actif',
    description TEXT
);

