-- =====================================================
-- SYSTÈME DE VOTE - TABLE CRÉATION
-- =====================================================
-- Exécuter ce script sur la base de données pidev
-- Pour ajouter la table de gestion des votes pour les propositions de réunion

-- Table vote (si elle n'existe pas)
CREATE TABLE IF NOT EXISTS `vote` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `vote` VARCHAR(50) NOT NULL COMMENT 'pour, contre, abstention',
  `voted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `proposition_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  
  -- Clé unique pour 1 seul vote par user par proposition
  UNIQUE KEY `unique_user_proposition_vote` (`user_id`, `proposition_id`),
  
  -- Index pour les recherches rapides
  KEY `idx_proposition_id` (`proposition_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- NOTES IMPORTANTES:
-- =====================================================
-- 1. La contrainte UNIQUE assure qu'un user ne peut voter qu'une fois par proposition
-- 2. Le champ 'vote' contient: 'pour', 'contre', ou 'abstention'
-- 3. Les abstentions sont exclues du calcul de majorité (voir VoteService.VoteStats)
-- 4. userId = 1 est actuellement hardcodé dans PropositionReunionController
-- 5. Le vote est actif tant que dateFinVote >= now() (voir PropositionReunionController)
-- 6. Logique de statut:
--    - > 50% pour → "Acceptée"
--    - > 50% contre → "Rejetée"  
--    - égalité → "Reportée"
--    - Pas de votes → "En attente"
