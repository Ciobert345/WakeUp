# Changelog

## [2.2.0] - 2026-02-17

### ✨ Nuove Funzionalità
- **Sistema di Aggiornamento Automatico**: L'app ora controlla automaticamente la disponibilità di nuove versioni su GitHub all'avvio
  - Notifica quando è disponibile un aggiornamento
  - Download diretto dell'APK con un solo tap
  - Barra di progresso in tempo reale durante il download
  - Installazione automatica al completamento

### 🐛 Correzioni Bug
- **Risolto problema di scheduling multiplo**: Gli allarmi programmati ora utilizzano ID univoci per evitare conflitti quando più PC sono schedulati allo stesso orario
- **Risolti problemi di encoding**: Tutti i caratteri italiani (è, à, ì, ò, ù) ora vengono visualizzati correttamente in tutta l'app
- **Corretta visualizzazione edge-to-edge**: Le schermate delle impostazioni ora utilizzano correttamente tutto lo schermo senza barre nere
- **Risolto crash della notifica di aggiornamento**: L'app non crasha più quando si clicca sulla notifica di aggiornamento

### 🎨 Miglioramenti UI
- Perfezionato il layout delle schermate di impostazione (Aspetto, Gestione Dati, Stabilità in Background)
- Rimosso padding superfluo per una migliore esperienza edge-to-edge
- Migliorata la visualizzazione dei messaggi di richiesta permessi

### 🔧 Miglioramenti Tecnici
- Aggiunto logging dettagliato per il sistema di aggiornamento
- Migliorato il parsing delle versioni per gestire formati non standard
- Ottimizzato il monitoraggio del progresso download
- Aggiornate le dipendenze di sistema

---

## [2.1.1] - Precedente

### Funzionalità Principali
- Schedulazione allarmi WOL precisi
- Gestione PC multipli
- Dashboard con statistiche
- Supporto per temi personalizzati
- Ottimizzazione batteria e permessi
