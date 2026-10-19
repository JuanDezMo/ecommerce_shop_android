#!/usr/bin/env python3
"""
Convierte los SVG de docs/imagenes-producto/ a vector drawables de Android (app/src/main/res/drawable/).

Solo entiende lo que usan esos 7 SVG: <rect> (con rx), <circle>, <path> y <g fill>, con fill, stroke,
stroke-width y stroke-linecap. El SVG es la fuente: si cambia, se vuelve a ejecutar este script.

Uso (desde la raíz del repositorio):  python3 scripts/svg_to_vector.py
"""
import re
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "docs" / "imagenes-producto"
TARGET = ROOT / "app" / "src" / "main" / "res" / "drawable"
SVG = "{http://www.w3.org/2000/svg}"


def num(value: float) -> str:
    return f"{value:g}"


def rect_path(x: float, y: float, w: float, h: float, r: float) -> str:
    if r <= 0:
        return f"M{num(x)},{num(y)}h{num(w)}v{num(h)}h{num(-w)}z"
    return (
        f"M{num(x + r)},{num(y)}h{num(w - 2 * r)}a{num(r)},{num(r)} 0 0 1 {num(r)},{num(r)}"
        f"v{num(h - 2 * r)}a{num(r)},{num(r)} 0 0 1 {num(-r)},{num(r)}"
        f"h{num(-(w - 2 * r))}a{num(r)},{num(r)} 0 0 1 {num(-r)},{num(-r)}"
        f"v{num(-(h - 2 * r))}a{num(r)},{num(r)} 0 0 1 {num(r)},{num(-r)}z"
    )


def circle_path(cx: float, cy: float, r: float) -> str:
    return f"M{num(cx - r)},{num(cy)}a{num(r)},{num(r)} 0 1 0 {num(2 * r)},0a{num(r)},{num(r)} 0 1 0 {num(-2 * r)},0z"


def paint(element, inherited_fill):
    fill = element.get("fill", inherited_fill)
    attributes = []
    if fill and fill != "none":
        attributes.append(f'android:fillColor="{fill}"')
    if element.get("stroke"):
        attributes.append(f'android:strokeColor="{element.get("stroke")}"')
        attributes.append(f'android:strokeWidth="{element.get("stroke-width", "1")}"')
        if element.get("stroke-linecap"):
            attributes.append(f'android:strokeLineCap="{element.get("stroke-linecap")}"')
    return attributes


def paths(element, inherited_fill=None):
    for child in element:
        tag = child.tag.replace(SVG, "")
        if tag == "g":
            yield from paths(child, child.get("fill", inherited_fill))
            continue
        if tag == "rect":
            data = rect_path(float(child.get("x", 0)), float(child.get("y", 0)), float(child.get("width")),
                             float(child.get("height")), float(child.get("rx", 0)))
        elif tag == "circle":
            data = circle_path(float(child.get("cx")), float(child.get("cy")), float(child.get("r")))
        elif tag == "path":
            data = re.sub(r"\s+", " ", child.get("d")).strip()
        else:
            raise ValueError(f"Elemento SVG no soportado: {tag}")
        yield paint(child, inherited_fill), data


def convert(svg_file: Path) -> str:
    root = ET.parse(svg_file).getroot()
    _, _, width, height = (float(v) for v in root.get("viewBox").split())
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        f"<!-- Generado por scripts/svg_to_vector.py desde docs/imagenes-producto/{svg_file.name}. No editar a mano -->",
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="256dp"',
        '    android:height="256dp"',
        f'    android:viewportWidth="{num(width)}"',
        f'    android:viewportHeight="{num(height)}">',
    ]
    for attributes, data in paths(root):
        lines.append("    <path")
        for attribute in attributes:
            lines.append(f"        {attribute}")
        lines.append(f'        android:pathData="{data}" />')
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


if __name__ == "__main__":
    TARGET.mkdir(parents=True, exist_ok=True)
    for svg in sorted(SOURCE.glob("product_*.svg")):
        (TARGET / f"{svg.stem}.xml").write_text(convert(svg))
        print("generado", f"{svg.stem}.xml")
