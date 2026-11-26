# StickerStudio: escopo do MVP

Versão 1, 26/11/2026. Apps nativos: iOS (Swift) e Android (Kotlin).

Fontes oficiais lidas para este documento (branch `main` do repo `WhatsApp/stickers`, em 26/11/2026):
[README geral](https://github.com/WhatsApp/stickers/blob/main/README.md),
[Android/README.md](https://github.com/WhatsApp/stickers/blob/main/Android/README.md),
[iOS/README.md](https://github.com/WhatsApp/stickers/blob/main/iOS/README.md),
[iOS `Interoperability.swift`](https://github.com/WhatsApp/stickers/blob/main/iOS/WAStickersThirdParty/Interoperability.swift) e
[iOS `Limits.swift`](https://github.com/WhatsApp/stickers/blob/main/iOS/WAStickersThirdParty/Limits.swift).
Contexto de áudio: a pesquisa interna `figurinhas-com-audio.md` (pasta de arquivos do projeto, fora do repo) e [issue #1163](https://github.com/WhatsApp/stickers/issues/1163).

---

## 1. Objetivo

Permitir que o usuário crie packs de figurinhas próprios (estáticos ou animados) a partir de fotos, vídeos e GIFs do aparelho e adicione esses packs ao WhatsApp pela API oficial de stickers de terceiros, com o mínimo de atrito.

O app é posicionado como **criador** de figurinhas, não como catálogo de packs prontos. Isso é também um requisito de loja: o README do iOS avisa que apps que só exportam stickers tendem a ser recusados pela Apple e recomenda "more functionality than to simply export stickers".

Figurinha com áudio **não entra no MVP**, porque a API de terceiros não suporta (resposta do mantenedor na issue #1163 em 24/11/2026: "on our roadmap"). A arquitetura deve deixar o áudio entrar sem retrabalho grande (seção 6).

Critério de pronto do MVP: um usuário consegue, nas duas plataformas, criar um pack estático e um animado do zero, adicioná-los ao WhatsApp, editar um pack já adicionado e ver a mudança refletida no WhatsApp.

## 2. Funcionalidades do MVP

### 2.1 Gestão de packs
- Criar pack com nome e autor (`name` e `publisher`, máx. 128 caracteres cada).
- O tipo do pack (estático ou animado) é escolhido na criação e fica travado. A API proíbe misturar os dois no mesmo pack.
- Listar, renomear, reordenar figurinhas e excluir packs e figurinhas.
- Indicar o estado do pack: rascunho (menos de 3 figurinhas), pronto, adicionado ao WhatsApp (só Android consegue detectar, ver 4.2).
- Limite de 30 figurinhas por pack, aplicado na UI.
- Limite de packs por app: ver decisão aberta D1.

### 2.2 Criação de figurinha estática
- Importar da galeria ou da câmera.
- Recorte e enquadramento num canvas quadrado, exportado em exatamente 512x512 com fundo transparente.
- Remoção automática de fundo, com borracha/pincel manual para corrigir.
  - iOS: Vision, `VNGenerateForegroundInstanceMaskRequest` (iOS 17+).
  - Android: ML Kit Subject Segmentation (API em beta no ML Kit; avaliar qualidade e tamanho do modelo antes de fechar).
- Texto sobreposto (fonte, cor, contorno) e contorno branco opcional na figurinha (o README recomenda 8 px #FFFFFF para legibilidade em fundos variados).

### 2.3 Criação de figurinha animada
- Importar vídeo ou GIF.
- Cortar trecho (máx. 10 s, que é o limite da API para a animação).
- Recorte quadrado, remoção de fundo opcional (frame a frame é caro; avaliar se entra no MVP ou fica para depois, ver D3).
- Texto sobreposto estático.
- Conversão para WebP animado com redução automática (fps, qualidade, duração) até caber em 500 KB. O usuário vê o resultado antes de salvar.
- Garantir que o primeiro frame seja representativo: o WhatsApp termina a animação no primeiro frame depois do loop, segundo o README.

### 2.4 Metadados
- 1 a 3 emojis por figurinha. Obrigatório no Android (`emojis` required), opcional no iOS. Para manter um modelo único, o app exige pelo menos 1 emoji nas duas plataformas, com sugestão padrão para não travar o fluxo.
- `accessibility_text` opcional (máx. 125 caracteres estático, 255 animado). O README pede inglês (EUA); no MVP o campo fica opcional e oculto num "avançado", ou vazio.
- Tray icon gerado automaticamente da primeira figurinha (96x96, máx. 50 KB), com opção de trocar.

### 2.5 Validação e exportação
- Validador local que roda antes de exportar, com as regras da seção 4.1, e mensagens específicas por erro (ex.: "figurinha 4 tem 612 KB, máx. 500 KB").
- Botão "Adicionar ao WhatsApp" por pack. A API proíbe ação de "adicionar todos".
- Detectar se o WhatsApp está instalado e tratar a ausência.
- Edição de pack já adicionado, com propagação para o WhatsApp (mecanismo diferente por plataforma, ver 4.2 e 4.3).

### 2.6 Dados e privacidade
- Tudo local no aparelho: sem conta, sem backend, sem upload de mídia.
- Política de privacidade publicada (exigida pelas duas lojas; também pode ir no campo `privacy_policy_website` do pack).

## 3. Fora de escopo do MVP

- **Figurinha com áudio.** Entra quando a Meta publicar a API na issue #1163.
- **Qualquer cliente não oficial** do WhatsApp (Baileys, protocolo do WhatsApp Web, patch de protobuf). Viola os termos, arrisca ban do usuário e quebra a cada atualização.
- Conta de usuário, sincronização em nuvem e backup.
- Compartilhar packs entre usuários, catálogo ou marketplace de packs prontos.
- Outros apps de mensagem (Telegram, iMessage sticker extension etc.).
- WhatsApp Web/Desktop como destino (a API de terceiros é só mobile).
- Editor de desenho livre (ilustração do zero).
- Monetização (ver D4: precisa ser decidida, mas não bloqueia o MVP técnico).

## 4. Requisitos técnicos da API do WhatsApp

### 4.1 Regras comuns (iguais nos READMEs de iOS e Android)

| Regra | Valor |
|---|---|
| Figurinhas por pack | mín. 3, máx. 30 |
| Tipo do pack | só estático ou só animado, nunca misto |
| Dimensão da figurinha | exatamente 512x512 px, fundo transparente |
| Formato | WebP (estático e animado). O iOS aceita PNG em arquivo, mas o JSON enviado ao WhatsApp exige WebP |
| Tamanho estático | ≤ 100 KB |
| Tamanho animado | ≤ 500 KB |
| Animação | frame mín. 8 ms, duração total ≤ 10 s |
| Tray icon | estático, 96x96 px, ≤ 50 KB (PNG) |
| `name`, `publisher` | ≤ 128 caracteres |
| `identifier` | < 128 caracteres, `a-z A-Z 0-9 _ - . espaço`, único |
| Emojis | máx. 3 por figurinha |
| `accessibility_text` | ≤ 125 (estático) / ≤ 255 (animado) |
| Adição | o usuário adiciona pack a pack, com botão próprio por pack |

Recomendação do README, não regra: figurinhas nativas do WhatsApp têm cerca de 15 KB. Quanto menor, melhor para envio.

Lojas:
- Não usar "WhatsApp" no nome do app nem no campo de nome da listagem. Pode citar na descrição.
- Palavra-chave `WAStickerApps` na descrição da Play Store e `WAStickers` nas keywords da App Store, para o app aparecer na busca de apps de figurinhas que o WhatsApp abre.
- Campos opcionais `android_play_store_link` e `ios_app_store_link`: preencher, porque quem recebe a figurinha pode tocar nela e cair na loja do app. É o principal canal orgânico previsto pela própria API.

### 4.2 Android

Mecanismo: o app expõe os packs num `ContentProvider` e dispara um intent para o WhatsApp.

- `ContentProvider` exportado, habilitado, com `android:readPermission="com.whatsapp.sticker.READ"`. Identificado pela authority; um pack é identificado pelo par authority + identifier.
- Quatro rotas obrigatórias:
  1. `<authority>/metadata`: todos os packs
  2. `<authority>/metadata/<pack_identifier>`: um pack
  3. `<authority>/stickers/<pack_identifier>`: figurinhas do pack (arquivo, emojis, acessibilidade)
  4. `<authority>/stickers_asset/<pack_identifier>/<sticker_file_name>`: binário, retornado como `AssetFileDescriptor`
- Como os packs são gerados pelo usuário, o provider serve arquivos do armazenamento interno (o README mostra o padrão `ParcelFileDescriptor` → `AssetFileDescriptor`), não de `assets/`. O provider lê do mesmo repositório local que o app (Room).
- Intent `com.whatsapp.intent.action.ENABLE_STICKER_PACK` com extras `sticker_pack_id`, `sticker_pack_authority`, `sticker_pack_name`, via `startActivityForResult`. Tratar `ActivityNotFoundException`. O usuário confirma num diálogo do WhatsApp.
- Checagem de pack adicionado (opcional, mas entra no MVP): `content://com.whatsapp.provider.sticker_whitelist_check/is_whitelisted?authority=...&identifier=...`, coluna `result` = 0/1, `null` se query inválida ou WhatsApp antigo. Para o WhatsApp Business a authority é `com.whatsapp.w4b.provider.sticker_whitelist_check`. O README recomenda manter o botão "adicionar" enquanto o pack não estiver nos dois apps.
- Atualização: campo `image_data_version` por pack. Incrementar a cada mudança de figurinha ou tray icon, inclusive quando o usuário edita um pack já adicionado.
- `avoid_cache` está deprecated (ignorado desde a versão 2.25.9.78 do WhatsApp). Não usar.
- Limite documentado: **1 a 10 packs por app** (ver D1).
- Encoding WebP: estático pode usar `Bitmap.CompressFormat.WEBP_LOSSY`/`WEBP_LOSSLESS` (API 30+). WebP animado não tem encoder no SDK: usar libwebp (`WebPAnimEncoder`, licença BSD) via NDK ou uma lib que a empacote.

### 4.3 iOS

Mecanismo: o app serializa o pack em JSON, coloca no pasteboard e abre o WhatsApp por URL scheme. Verificado no README e no `Interoperability.swift` oficiais.

- `Info.plist`: `LSApplicationQueriesSchemes` com `whatsapp`, para `canOpenURL(whatsapp://)` funcionar.
- JSON do pack:
  ```json
  {
    "ios_app_store_link": "String",
    "android_play_store_link": "String",
    "identifier": "String",
    "name": "String",
    "publisher": "String",
    "tray_image": "base64 do PNG (não WebP)",
    "stickers": [
      { "image_data": "base64 do WebP (não PNG)", "emojis": ["…"], "accessibility_text": "…" }
    ]
  }
  ```
  Pack animado precisa de `animated_sticker_pack: true` (descrito no README para o arquivo `.wasticker`; confirmar na implementação de referência que o campo vai no JSON do pasteboard).
- Pasteboard (código oficial): `UIPasteboard.general.setItems([["net.whatsapp.third-party.sticker-pack": data]], options: [.localOnly: true, .expirationDate: agora + 60 s])` e depois `UIApplication.shared.open(URL("whatsapp://stickerPack"))`.
- Um pack por envio.
- O bundle id não pode conter `WA.WAStickersThirdParty` (o sample faz `fatalError` nesse caso).
- **Não existe** checagem de "pack já adicionado" no iOS. A UI do iOS não mostra esse estado; mostra só "enviado ao WhatsApp em <data>" a partir de registro local.
- **Atualização de pack já adicionado não está documentada no iOS** (não há `image_data_version`). Hipótese a validar em aparelho: reenviar com o mesmo `identifier` substitui o pack. Se não substituir, a edição no iOS gera um novo identifier e o usuário remove o antigo manualmente. Isso precisa ser testado antes de fechar o fluxo de edição.
- Não há limite de packs por app documentado no README do iOS.
- Encoding: o ImageIO decodifica WebP (iOS 14+) mas não codifica. O sample usa `WebP.framework` (libwebp) + YYImage. Recomendação: libwebp via SPM (encoder estático e `WebPAnimEncoder`), sem YYImage, que está sem manutenção.
- App Store: o próprio WhatsApp alerta para recusa de apps que só exportam figurinhas e para não reaproveitar a UI do sample. O valor do app precisa estar no editor (seções 2.2 e 2.3).

### 4.4 Implicação para paridade
O modelo de dados e o validador são os mesmos nas duas plataformas, mas três comportamentos divergem e a UI precisa refletir isso: estado "adicionado" (só Android), atualização de pack (Android documentado, iOS a validar) e limite de packs (só Android documenta).

Sugestão: manter as regras da 4.1 num arquivo único versionado no repo (ex.: `spec/whatsapp-limits.json`) e gerar ou testar os validadores de Swift e Kotlin contra ele, para os dois apps não divergirem.

## 5. Arquitetura proposta (MVP)

Mesma divisão em camadas nas duas plataformas, com nomes equivalentes para facilitar revisão cruzada.

| Camada | Responsabilidade | iOS | Android |
|---|---|---|---|
| UI | telas, editor | SwiftUI | Jetpack Compose |
| Domínio | `StickerPack`, `Sticker`, validador, casos de uso | Swift puro | Kotlin puro |
| Mídia | decode, recorte, segmentação, encode WebP | Vision, Core Image, AVFoundation, libwebp | ML Kit, MediaCodec/MediaMetadataRetriever, libwebp (NDK) |
| Persistência | metadados + arquivos | SwiftData ou Core Data + arquivos no container do app | Room + arquivos em `filesDir` |
| Exportação | integração WhatsApp | `PasteboardExporter` | `ContentProvider` + `IntentExporter` |

Pontos de decisão técnicos:
- Persistência guarda **duas versões** de cada figurinha: o asset final exportável (WebP 512x512) e a fonte editável (imagem original ou trecho de vídeo + parâmetros de edição). Sem a fonte, editar texto ou recorte depois exige refazer do zero. Isso é também o que viabiliza o áudio depois (6.2).
- Evitar FFmpeg no MVP: aumenta muito o binário e traz questões de licença (LGPL/GPL conforme o build). AVFoundation e MediaCodec cobrem extração de frames; libwebp cobre o encode.
- Pipeline de encode com busca de parâmetros (qualidade, fps, escala temporal) até caber no limite, rodando fora da main thread e com cancelamento.

## 6. Preparação para figurinha com áudio

O formato oficial ainda não existe. O que se sabe por engenharia reversa (WebP animado + Ogg/Opus num campo novo do protobuf) não é contrato e não deve guiar implementação. A preparação, então, é estrutural, sem codificar suposições de formato.

### 6.1 O que fazer já no MVP (custo baixo)
1. **Exportador atrás de interface com capacidades.** Ex.: `protocol StickerPackExporter { var capabilities: ExportCapabilities; func export(_ pack: StickerPack) }`, com `ExportCapabilities.supportsAudio = false`. Quando a API sair, muda a implementação e a flag, não a UI inteira.
2. **Tipo de pack como enum extensível**, não booleano: `PackKind { static, animated }`. O `animated_sticker_pack: Bool` da API fica só no mapeamento de exportação. Se a Meta criar um terceiro tipo (ou exigir pack só de figurinhas com áudio, como acontece hoje com estático vs. animado), entra um caso novo.
3. **Guardar o trecho de vídeo fonte com a trilha de áudio original** nas figurinhas animadas criadas a partir de vídeo (o trecho cortado, ≤ 10 s, não o vídeo inteiro). Isso permite oferecer "adicionar o som original" retroativamente em packs que o usuário já tem. Trade-off: mais armazenamento; medir e oferecer limpeza.
4. **Migrações de schema desde a versão 1** (Room migrations / versão de modelo no SwiftData ou Core Data), para que um campo `audio` opcional entre sem perda de dados.
5. **Módulo de mídia separado por tipo** (`ImageEncoder`, e no futuro `AudioEncoder`), para o encoder de áudio entrar como peça nova.
6. **Feature flag** para a área de áudio, desligada por padrão, para poder testar internamente quando houver spec sem expor ao público.

### 6.2 O que fazer quando a Meta publicar a API
- Ler os requisitos que o mantenedor disse que vai postar na issue #1163 (codec, container, duração, tamanho, formato do pack, mudanças no `ContentProvider` e no JSON do pasteboard).
- Adicionar `audio: AudioClip?` ao `Sticker` (via migração), o editor de som (cortar, gravar voz, usar o som original do vídeo) e o `AudioEncoder`.
- Codec: se for Opus, há encoder nativo no Android via MediaCodec (Android 10+) e no iOS via AudioToolbox/AVAudioConverter. O container (Ogg ou outro) depende da spec e pode exigir lib extra. Verificar na hora, não antes.

### 6.3 Acompanhamento
- Monitorar a issue #1163 (inscrição no GitHub ou um watch periódico). É o único ponto onde a Meta disse que publicará os requisitos.

## 7. Decisões em aberto

| # | Decisão | Recomendação |
|---|---|---|
| D1 | Limite de packs. O README do Android documenta 1 a 10 packs por app. Não está claro se o WhatsApp força isso para packs dinâmicos via `ContentProvider` | Aplicar limite de 10 packs no Android no MVP e testar em aparelho se o WhatsApp rejeita o 11º. No iOS, sem limite documentado, usar o mesmo limite por paridade até ter dado |
| D2 | Versões mínimas de SO | iOS 17+ (segmentação do Vision usada no recorte). Android: definir após checar requisitos do ML Kit Subject Segmentation |
| D3 | Remoção de fundo em figurinha animada | Fora do MVP; entra no MVP só a estática. É cara (frame a frame) e a qualidade em vídeo precisa ser testada |
| D4 | Modelo de monetização | Decisão de produto do usuário; não bloqueia o desenvolvimento do MVP |
| D5 | Atualização de pack no iOS | Validar em aparelho antes de fechar o fluxo de edição (ver 4.3) |

## 8. Riscos

- **Recusa na App Store** por funcionalidade mínima ou por parecer o sample oficial. Mitigação: editor como centro do produto, UI própria.
- **Janela da demanda por áudio.** O MVP não entrega o que motivou o projeto. O valor inicial é ser um bom criador de packs, e o áudio depende de um prazo da Meta que não existe publicamente.
- **Mudança da API pela Meta** quando o áudio chegar (pode mexer em formato de pack, não só adicionar campo). Mitigação: exportador isolado (6.1).
- **Qualidade x 500 KB** em animadas: vídeos com muito movimento podem ficar ruins no limite. Mitigação: preview e ajuste antes de salvar.
