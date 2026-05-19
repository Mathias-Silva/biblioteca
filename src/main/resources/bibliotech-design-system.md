# BiblioTech — Design System

Documento de referência para o agente de IA aplicar o design system no projeto. Leia tudo antes de alterar qualquer arquivo.

---

## Paleta de cores

### Primária — Indigo
| Token | Hex | Uso |
|---|---|---|
| `indigo-50` | `#EEF2FF` | Fundo de badges ISBN, alertas suaves |
| `indigo-200` | `#C7D2FE` | Borda de alertas info |
| `indigo-400` | `#818CF8` | Hover de links |
| `indigo-500` | `#6366F1` | Cor principal da marca, ícones, links ativos |
| `indigo-600` | `#4F46E5` | Botão primário (fundo) |
| `indigo-700` | `#4338CA` | Botão primário hover |
| `indigo-800` | `#3730A3` | Texto sobre fundo indigo-50 |

### Neutros
| Token | Hex | Uso |
|---|---|---|
| `gray-50` | `#F9FAFB` | **Hover de linha da tabela** |
| `gray-100` | `#F3F4F6` | Cabeçalho da tabela, fundo de seções |
| `gray-200` | `#E5E7EB` | Bordas |
| `gray-400` | `#9CA3AF` | Texto desabilitado / placeholder |
| `gray-700` | `#374151` | Texto corpo |
| `gray-900` | `#111827` | Navbar, headings principais |

### Semânticas
| Token | Hex | Uso |
|---|---|---|
| `success` | `#10B981` | Confirmações |
| `warning` | `#F59E0B` | Alertas |
| `danger` | `#EF4444` | Botão sair, ícone lixeira, erros |
| `danger-light` | `#FCA5A5` | Borda do botão lixeira |
| `danger-bg` | `#FEF2F2` | Hover fundo do botão lixeira |

---

## Tipografia

Fonte: sistema (`-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`)  
Pesos: **400** (regular) e **500** (medium) / **600** (apenas h1 de página)

| Nível | Tamanho | Peso | Cor | Uso |
|---|---|---|---|---|
| Page title | `20px` | `600` | `gray-900` | "Biblioteca Pessoal" |
| Section heading | `15px` | `500` | `gray-900` | Títulos de seção |
| Body | `14px` | `400` | `gray-700` | Texto geral |
| Body muted | `13px` | `400` | `gray-400` | Subtítulo, autor, descrições |
| Label | `11px` | `600` | `gray-400` | Cabeçalhos de tabela (uppercase + letter-spacing 0.06em) |
| Badge | `12px` | `500` | `indigo-800` | Badges ISBN |

---

## Componentes

### Navbar

```css
background: #111827;        /* gray-900 */
height: 52px;
padding: 0 24px;
display: flex;
align-items: center;
justify-content: space-between;
```

- **Logo**: ícone 22px + texto "BiblioTech" em branco, peso 600, gap 8px
- **Link "Área do Usuário"**: cor `#6366F1` (indigo-500), tamanho 13px, peso 500
- **Botão Sair**: fundo transparente, cor `#F87171`, borda `1px solid #EF4444`, border-radius 8px, padding `5px 12px`, font-size 13px

---

### Botão primário — "Adicionar Livro"

```css
background: #4F46E5;        /* indigo-600 — NÃO usar branco */
color: #ffffff;
border: none;
border-radius: 10px;
padding: 10px 20px;
font-size: 14px;
font-weight: 500;
display: inline-flex;
align-items: center;
gap: 7px;
cursor: pointer;
transition: background 0.2s ease, transform 0.15s ease;
```

**Hover:**
```css
background: #4338CA;        /* indigo-700 */
transform: translateY(-1px);
```

**Active:**
```css
transform: scale(0.98);
```

> ⚠️ **Bug conhecido**: o botão está aparecendo branco. Verificar se há `background: white` ou `background: var(--color-background-primary)` sobrescrevendo. Garantir que o seletor do botão primário tem especificidade suficiente.

---

### Botão fantasma (ghost)

```css
background: transparent;
color: #374151;             /* gray-700 */
border: 0.5px solid #E5E7EB; /* gray-200 */
border-radius: 8px;
padding: 7px 14px;
font-size: 13px;
cursor: pointer;
transition: background 0.2s ease, border-color 0.2s ease;
```

**Hover:**
```css
background: #F3F4F6;        /* gray-100 */
border-color: #9CA3AF;      /* gray-400 */
```

---

### Botão ícone (icon-btn)

```css
width: 30px;
height: 30px;
border-radius: 7px;
border: 0.5px solid #E5E7EB;
display: flex;
align-items: center;
justify-content: center;
cursor: pointer;
background: transparent;
color: #374151;
font-size: 15px;
transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
```

**Hover (editar):**
```css
background: #F3F4F6;
border-color: #9CA3AF;
```

**Variante danger (lixeira):**
```css
color: #DC2626;
border-color: #FCA5A5;
```

**Hover danger:**
```css
background: #FEF2F2;
border-color: #EF4444;
```

---

### Tabela de livros

**Wrapper:**
```css
background: #ffffff;
border-radius: 12px;
border: 0.5px solid #E5E7EB;
overflow: hidden;
```

**Cabeçalho `<thead>`:**
```css
background: #F3F4F6;        /* gray-100 */
color: #9CA3AF;             /* gray-400 */
font-size: 11px;
font-weight: 600;
letter-spacing: 0.06em;
text-transform: uppercase;
padding: 10px 16px;
border-bottom: 0.5px solid #E5E7EB;
```

**Linha `<tr>` — estado padrão:**
```css
background: #ffffff;
transition: background 0.15s ease;
```

**Linha `<tr>` — hover:**
```css
background: #F9FAFB;        /* gray-50 — NÃO usar branco */
```

> ⚠️ **Bug conhecido**: o hover da linha está ficando branco (invisível sobre fundo branco). Trocar de `background: #ffffff` ou `background: white` para `background: #F9FAFB` no estado `:hover`.

**Célula `<td>`:**
```css
padding: 14px 16px;
color: #374151;
border-bottom: 0.5px solid #E5E7EB;
vertical-align: middle;
```

**Última linha — sem borda inferior:**
```css
tbody tr:last-child td {
  border-bottom: none;
}
```

---

### Badge ISBN

```css
display: inline-flex;
align-items: center;
background: #EEF2FF;        /* indigo-50 */
color: #3730A3;             /* indigo-800 */
border-radius: 20px;
padding: 3px 10px;
font-size: 12px;
font-weight: 500;
```

---

### Inputs de formulário

```css
width: 100%;
border: 0.5px solid #E5E7EB;
border-radius: 10px;
padding: 10px 14px;
font-size: 14px;
color: #374151;
background: #ffffff;
outline: none;
transition: border-color 0.15s ease, box-shadow 0.15s ease;
```

**Focus:**
```css
border-color: #6366F1;
box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
```

**Label acima do input:**
```css
font-size: 12px;
font-weight: 500;
color: #9CA3AF;
margin-bottom: 5px;
```

---

### Alerta de sessão encerrada

```css
display: flex;
align-items: center;
gap: 8px;
background: #EEF2FF;        /* indigo-50 */
border: 0.5px solid #C7D2FE; /* indigo-200 */
border-radius: 10px;
padding: 10px 14px;
font-size: 13px;
color: #4338CA;             /* indigo-700 */
margin-bottom: 20px;
```

Incluir ícone `info-circle` (Tabler Icons outline) 16px à esquerda.

---

### Card de login

```css
background: #ffffff;
border-radius: 16px;
border: 0.5px solid #E5E7EB;
padding: 32px 36px;
width: 360px;
margin: 0 auto;
```

---

## Animações & transições

Regra geral: **todas as interações de hover e active devem ter `transition` suave**.

```css
/* Aplicar em todos os botões e linhas interativas */
transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease, transform 0.15s ease;
```

| Elemento | Propriedades animadas | Duração |
|---|---|---|
| Botão primário | `background`, `transform` | `0.2s ease` |
| Botão ghost | `background`, `border-color` | `0.2s ease` |
| Botão ícone | `background`, `border-color`, `color` | `0.2s ease` |
| Linha da tabela | `background` | `0.15s ease` |
| Input focus | `border-color`, `box-shadow` | `0.15s ease` |

---

## Bugs para corrigir

### 1. Hover da linha da tabela transparente
**Problema:** `tr:hover` está com `background: white` ou não definido, tornando o efeito invisível.  
**Correção:**
```css
tbody tr:hover td {
  background: #F9FAFB; /* gray-50 */
}
```

### 2. Botão "Adicionar Livro" branco
**Problema:** algum estilo global ou variável CSS está sobrescrevendo o fundo do botão primário com branco.  
**Correção:** garantir que o seletor do botão tenha especificidade suficiente e use `background: #4F46E5 !important` se necessário em último caso, ou revisar a cadeia de herança de `--color-background-primary`.

### 3. Botões sem animação de hover
**Problema:** ausência de `transition` nos botões.  
**Correção:** adicionar em todos os botões:
```css
transition: background 0.2s ease, border-color 0.2s ease, transform 0.15s ease;
```
E no hover do botão primário adicionar `transform: translateY(-1px)`.

---

## Ícones

Usar **Tabler Icons** (outline only). Carregar via CDN:
```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@tabler/icons-webfont@latest/tabler-icons.min.css" />
```

Uso: `<i class="ti ti-NOME" aria-hidden="true"></i>`

| Ícone | Classe | Uso |
|---|---|---|
| Lápis / editar | `ti-pencil` | Botão editar linha |
| Lixeira | `ti-trash` | Botão excluir linha |
| Mais | `ti-plus` | Botão adicionar livro |
| Usuário | `ti-user` | Link área do usuário |
| Logout | `ti-logout` | Botão sair |
| Info | `ti-info-circle` | Alerta de sessão |
| Filtro | `ti-filter` | Botão filtrar (se existir) |

Tamanho padrão inline: `font-size: 15px` ou `16px`.

---

## Logo / marca

```html
<!-- SVG embutido — sem dependência externa -->
<svg width="24" height="24" viewBox="0 0 26 26" fill="none" xmlns="http://www.w3.org/2000/svg">
  <rect width="26" height="26" rx="7" fill="#6366F1"/>
  <path d="M7 7h5a4 4 0 0 1 4 4v8H7V7Z" fill="white" opacity="0.9"/>
  <path d="M16 11h2a3 3 0 0 1 3 3v5h-5v-8Z" fill="white" opacity="0.55"/>
</svg>
```

Texto ao lado: "BiblioTech", peso 700, tamanho 16px, cor branca na navbar.

---

## Checklist de implementação

- [ ] Hover das linhas da tabela: `background: #F9FAFB` (não branco)
- [ ] Botão primário: `background: #4F46E5`, cor branca — remover qualquer override branco
- [ ] Todos os botões: adicionar `transition: background 0.2s ease, ...`
- [ ] Botão primário hover: `background: #4338CA` + `transform: translateY(-1px)`
- [ ] Botão ícone danger hover: `background: #FEF2F2`
- [ ] Inputs: borda `0.5px solid #E5E7EB`, focus com ring indigo
- [ ] Badge ISBN: fundo `#EEF2FF`, texto `#3730A3`
- [ ] Alerta de login: fundo `#EEF2FF`, borda `#C7D2FE`, texto `#4338CA`
- [ ] Tabler Icons carregado via CDN
