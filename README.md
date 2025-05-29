# 🔥 Firestore Developer Reviewer

Aplicativo Android desenvolvido em **Kotlin + Jetpack Compose**, utilizando **Firestore** como banco de dados.

## 🚀 Tecnologias

- Kotlin
- Jetpack Compose
- Firebase Firestore
- Coroutines + Flow
- Koin

## ☁️ Firestore

O projeto utiliza o **Firestore Database** como backend, com a seguinte estrutura:

**Collection:** `developers`

**Documento:**
- `id`: gerado automaticamente ou manual
- `name`: `String`
- `idGit`: `String`
- `idSlack`: `String`
- `availableForReview`: `Boolean`

### Exemplo de documento:
```json
{
  "name": "João Silva",
  "idGit": "joaosilva",
  "idSlack": "joao_silva",
  "
