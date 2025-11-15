# 🔥 BACKLOG COMPLET – Payment & Billing System

---

## 🟪 EPIC A — Gestion des paiements (PaymentService – Création & stockage)

### 🟣 Story A1 — Créer un paiement

**En tant que** client (appli front ou autre service)
**Je veux** créer un paiement
**Afin de** démarrer une transaction pour une commande/utilisateur

**Tâches :**

* [ ] Définir le modèle de requête de création de paiement :
  `amount`, `currency`, `method`, `userId`, `orderId` (optionnel)
* [ ] Valider les données d’entrée :

    * montant > 0
    * devise non vide
    * méthode de paiement autorisée
    * userId obligatoire
* [ ] Créer un paiement avec :

    * statut initial = `PENDING`
    * `createdAt` et `updatedAt`
* [ ] Sauvegarder le paiement en base
* [ ] Retourner le paiement avec son identifiant unique

---

### 🟣 Story A2 — Empêcher les doublons de création (optionnel)

**En tant que** système
**Je veux** éviter de créer deux paiements identiques par erreur
**Afin de** ne pas facturer deux fois la même action

**Tâches :**

* [ ] Définir une logique d’« idempotency » (ex : `orderId` + `userId`)
* [ ] Si un paiement existe déjà pour la même commande encore en `PENDING` :

    * retourner le paiement existant au lieu d’en créer un nouveau
* [ ] Loguer les cas de duplication détectée

---

## 🟦 EPIC B — Cycle de vie des paiements (statuts & règles métier)

### 🟣 Story B1 — Consulter un paiement par ID

**En tant que** utilisateur / administrateur / autre service
**Je veux** récupérer un paiement par identifiant
**Afin de** voir son état et ses détails

**Tâches :**

* [ ] Récupérer un paiement par son ID
* [ ] Retourner 404 si non trouvé
* [ ] Retourner tous les champs utiles (montant, statut, dates, méthode, userId, orderId)

---

### 🟣 Story B2 — Lister les paiements avec filtres

**En tant que** administrateur
**Je veux** lister les paiements
**Afin de** superviser l’activité et filtrer les résultats

**Tâches :**

* [ ] Permettre de lister tous les paiements
* [ ] Ajouter des filtres optionnels :

    * par `status`
    * par `userId`
    * par `orderId`
* [ ] Gérer la pagination (page, taille de page)

---

### 🟣 Story B3 — Mettre à jour le statut d’un paiement

**En tant que** système ou back-office
**Je veux** mettre à jour le statut d’un paiement
**Afin de** refléter le résultat réel de la transaction

**Tâches :**

* [ ] Définir les statuts possibles :

    * `PENDING`, `APPROVED`, `FAILED`, `CANCELED`
* [ ] Définir les transitions autorisées :

    * `PENDING` → `APPROVED`
    * `PENDING` → `FAILED`
    * `PENDING` → `CANCELED`
    * aucun changement possible après `APPROVED/FAILED/CANCELED`
* [ ] Implémenter le changement de statut
* [ ] Mettre à jour `updatedAt`
* [ ] Retourner le paiement mis à jour

---

### 🟣 Story B4 — Rejeter les transitions invalides

**En tant que** système
**Je veux** refuser les transitions illégales
**Afin de** garantir l’intégrité du cycle de vie

**Tâches :**

* [ ] Centraliser les règles de transitions dans la couche métier
* [ ] Retourner une erreur claire (ex : 409) si :

    * on essaie de modifier un paiement déjà `APPROVED`, `FAILED` ou `CANCELED`
    * le statut cible est inconnu
* [ ] Loguer ces erreurs métier pour diagnostic

---

## 🟩 EPIC C — Facturation (BillingService – Création & gestion des factures)

### 🟣 Story C1 — Créer une facture à partir d’un paiement approuvé

**En tant que** système de facturation
**Je veux** créer une facture quand un paiement est approuvé
**Afin de** garder une trace légale/comptable

**Tâches :**

* [ ] Définir l’entité `Invoice` avec :

    * `id`, `invoiceNumber`, `paymentId`, `userId`, `amount`, `currency`, `status`, `issueDate`, `dueDate` (optionnel), éventuellement `orderId`
* [ ] Définir les statuts de facture :

    * `CREATED`, `SENT`, `PAID`, `CANCELLED`
* [ ] Créer une facture en statut `CREATED` à partir d’un paiement approuvé
* [ ] Sauvegarder la facture en base
* [ ] Générer un `invoiceNumber` unique (ex : horodatage + séquence)

---

### 🟣 Story C2 — Consulter une facture

**En tant que** utilisateur / admin
**Je veux** récupérer une facture
**Afin de** consulter les détails de la facturation

**Tâches :**

* [ ] Récupérer une facture par `id` ou par `paymentId`
* [ ] Retourner 404 si non trouvée
* [ ] Inclure dans la réponse : montant, devise, dates, statut, références paiement

---

### 🟣 Story C3 — Lister les factures

**En tant que** administrateur
**Je veux** lister les factures
**Afin de** suivre la facturation globale

**Tâches :**

* [ ] Lister toutes les factures
* [ ] Ajouter des filtres :

    * par `userId`
    * par `status`
    * par période (date de création)
* [ ] Gérer pagination

---

### 🟣 Story C4 — Mettre à jour le statut d’une facture

**En tant que** équipe finance / système
**Je veux** mettre à jour le statut d’une facture
**Afin de** refléter son état réel (envoyée, payée, annulée)

**Tâches :**

* [ ] Définir les transitions autorisées (ex : `CREATED` → `SENT` → `PAID`)
* [ ] Empêcher les retours à un statut antérieur illogique
* [ ] Mettre à jour les dates si nécessaire (`paidAt`, etc.)

---

## 🟧 EPIC D — Intégration PaymentService ↔ BillingService

### 🟣 Story D1 — Créer automatiquement une facture après un paiement approuvé

**En tant que** système
**Je veux** que la facturation se déclenche automatiquement quand un paiement est approuvé
**Afin de** éviter la saisie manuelle et les oublis

**Tâches :**

* [ ] Dans PaymentService, après passage à `APPROVED`, appeler BillingService :

    * ex : `POST /invoices` avec les données nécessaires
* [ ] Envoyer :

    * `paymentId`, `amount`, `currency`, `userId`, `orderId` (si existant), date du paiement
* [ ] Recevoir et stocker la réponse de BillingService (au moins `invoiceId`)
* [ ] Facultatif : stocker une référence `invoiceId` coté paiement

---

### 🟣 Story D2 — Gérer les erreurs de communication vers BillingService

**En tant que** système
**Je veux** que l’échec de la création de facture ne casse pas la logique de paiement
**Afin de** ne pas bloquer un paiement valide à cause d’un problème de facturation

**Tâches :**

* [ ] Si l’appel à BillingService échoue :

    * garder le paiement en `APPROVED`
    * loguer l’erreur (technique + payload)
    * marquer le paiement comme “facture à créer” (flag)
* [ ] Prévoir un mécanisme pour rejouer la création de facture plus tard (batch, tâche planifiée ou endpoint d’admin)

---

### 🟣 Story D3 — Consulter l’état de facturation d’un paiement

**En tant que** administrateur
**Je veux** savoir si une facture est liée à un paiement
**Afin de** vérifier la cohérence entre paiements et facturation

**Tâches :**

* [ ] Ajouter dans PaymentService un moyen de récupérer :

    * l’info facture liée (invoiceId, status)
* [ ] Interroger BillingService si nécessaire pour rafraîchir le statut de facture
* [ ] Gérer les cas où la facture n’existe pas ou a été supprimée

---

## 🟫 EPIC E — Validation, erreurs et règles transverses

### 🟣 Story E1 — Validation des données d’entrée

**En tant que** backend
**Je veux** valider toutes les données d’entrée
**Afin de** garantir la cohérence et la sécurité

**Tâches :**

* [ ] Validation des champs de création de paiement
* [ ] Validation des modifications de statut
* [ ] Validation des champs de création de facture

---

### 🟣 Story E2 — Gestion d’erreurs cohérente

**En tant que** consommateur d’API
**Je veux** des réponses d’erreur homogènes
**Afin de** simplifier l’intégration et le debug

**Tâches :**

* [ ] Définir un format de réponse d’erreur standard (code, message, détails)
* [ ] Gérer :

    * 400 : entrée invalide
    * 404 : paiement ou facture introuvable
    * 409 : violation de règles métier (ex : transition de statut interdite)
    * 500 : erreur interne
* [ ] Loguer systématiquement les erreurs métier et techniques importantes

---

## 🟨 EPIC F — Reporting & suivi (optionnel mais utile)

### 🟣 Story F1 — Résumé simple des paiements

**En tant que** admin
**Je veux** un résumé agrégé des paiements
**Afin de** suivre l’activité (montant total, nombre de paiements par statut)

**Tâches :**

* [ ] Fournir un endpoint type `/payments/summary`
* [ ] Calculer :

    * nombre de paiements par statut
    * total montants approuvés sur une période
* [ ] Paramètres : période (date début / date fin)

---

### 🟣 Story F2 — Résumé de facturation

**En tant que** équipe finance
**Je veux** un état des factures
**Afin de** suivre ce qui est facturé, payé, en attente

**Tâches :**

* [ ] Endpoint `/invoices/summary`
* [ ] Regrouper par statut (`CREATED`, `SENT`, `PAID`, `CANCELLED`)
* [ ] Possibilité de filtrer par userId ou par période

---

## 🟦 EPIC G — Tests fonctionnels & qualité

### 🟣 Story G1 — Tester les scénarios de paiement

**Tâches :**

* [ ] Scénario : création de paiement valide
* [ ] Scénario : paiement invalide (montant négatif, method inconnue…)
* [ ] Scénario : mise à jour de statut valide
* [ ] Scénario : tentative de transition interdite (ex : `APPROVED` → `PENDING`)

---

### 🟣 Story G2 — Tester l’intégration Payment ↔ Billing

**Tâches :**

* [ ] Scénario complet :

    * création paiement `PENDING`
    * passage en `APPROVED`
    * création automatique d’une facture
* [ ] Scénario : BillingService indisponible → paiement reste `APPROVED` mais facture non créée
* [ ] Scénario : re-création de facture sur paiement marqué “à facturer”
