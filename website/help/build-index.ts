import * as fs from "fs";
import * as path from "path";

const HELP_HTML_DIR = __dirname;
const TOC_XML = path.join(__dirname, "../../doc/helpjar/toc.xml");
const MAP_XML = path.join(__dirname, "../../doc/helpjar/default.map");

// Parse default.map to get target -> url mapping
function parseMap(xml: string): Map<string, string> {
  const map = new Map<string, string>();
  const re = /<mapID\s+target="([^"]+)"\s+url="([^"]+)"/g;
  let m;
  while ((m = re.exec(xml)) !== null) {
    const target = m[1];
    let url = m[2];
    // Resolve relative to doc/helpjar, so html/Foo.html becomes website/help/Foo.html
    const resolved = path.resolve(path.join(__dirname, "../../doc/helpjar"), url);
    // Check if the file exists
    if (fs.existsSync(resolved)) {
      // Get relative path from website/help
      const relPath = path.relative(HELP_HTML_DIR, resolved);
      map.set(target, relPath);
    }
  }
  return map;
}

interface TocItem {
  text: string;
  target?: string;
  children: TocItem[];
}

function parseTocXml(xml: string): TocItem[] {
  const result: TocItem[] = [];
  const re = /([ \t]*)<tocitem\b([^>]*)>/g;
  const closeRe = /([ \t]*)<\/tocitem>/g;

  // We'll extract all tocitems with their indent level
  const entries: { indent: number; text: string; target?: string; isClose: boolean }[] = [];

  let m;
  while ((m = re.exec(xml)) !== null) {
    const indent = m[1].length;
    const attrs = m[2];
    const textMatch = attrs.match(/text="([^"]*)"/);
    const targetMatch = attrs.match(/target="([^"]*)"/);
    entries.push({
      indent,
      text: textMatch ? textMatch[1] : "",
      target: targetMatch ? targetMatch[1] : undefined,
      isClose: false,
    });
  }

  // Also capture closes (we'll just use a stack)
  const stack: { item: TocItem; indent: number }[] = [];

  for (const entry of entries) {
    const item: TocItem = { text: entry.text, target: entry.target, children: [] };

    // Pop items with same or less indent
    while (stack.length > 0 && stack[stack.length - 1].indent >= entry.indent) {
      const popped = stack.pop()!;
      if (stack.length > 0) {
        stack[stack.length - 1].item.children.push(popped.item);
      } else {
        result.push(popped.item);
      }
    }
    stack.push({ item, indent: entry.indent });
  }

  // Pop remaining
  while (stack.length > 0) {
    const popped = stack.pop()!;
    if (stack.length > 0) {
      stack[stack.length - 1].item.children.push(popped.item);
    } else {
      result.push(popped.item);
    }
  }

  return result;
}

const mapData = parseMap(fs.readFileSync(MAP_XML, "utf-8"));
const toc = parseTocXml(fs.readFileSync(TOC_XML, "utf-8"));

function resolveTarget(target: string | undefined): string | undefined {
  if (!target) return undefined;
  // Look up in the map
  const url = mapData.get(target);
  if (url) return url;
  // Fallback: replace _html with .html
  const fallback = target.replace(/_html$/, ".html");
  if (fs.existsSync(path.join(HELP_HTML_DIR, fallback))) {
    return fallback;
  }
  return undefined;
}

function hasAnyLink(item: TocItem): boolean {
  if (item.target && resolveTarget(item.target)) return true;
  for (const c of item.children) {
    if (hasAnyLink(c)) return true;
  }
  return false;
}

function buildBody(items: TocItem[], level = 0): string {
  let html = "";

  for (const item of items) {
    if (!hasAnyLink(item)) continue;

    if (item.target) {
      const url = resolveTarget(item.target);
      if (url) {
        html += `<li><a href="${url}">${item.text}</a></li>\n`;
      } else {
        // Group item with no page — render as heading
        html += `</ul>\n<h${level + 2}>${item.text}</h${level + 2}>\n<ul>\n`;
      }
    } else {
      // No target at all — heading
      html += `</ul>\n<h${level + 2}>${item.text}</h${level + 2}>\n<ul>\n`;
    }

    if (item.children.length > 0) {
      html += buildBody(item.children, level + 1);
    }
  }
  return html;
}

const body = buildBody(toc);

const fullHtml = `<!DOCTYPE html>
<html lang="sv">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Bokfri — Hjälp</title>
<link rel="stylesheet" href="style.css">
</head>
<body>
<h1>Bokfri Hjälp</h1>
<p>Klicka på ett ämne för att läsa mer.</p>
<ul>
${body}
</ul>
</body>
</html>
`;

fs.writeFileSync(path.join(HELP_HTML_DIR, "index.html"), fullHtml);
console.log("Generated help/index.html with links");