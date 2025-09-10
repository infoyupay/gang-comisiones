---
apply: manually
---

- todos los javadocs se escriben completamente en inglés, incluso si el código documentado está en español.
- en el javadoc a nivel de clase o package-info agrega @author InfoYupay SACS y @version 1.0
- separador de párrafos: no uses <p>, usa <br/>
- no uses <code>, usa {@code}
- si usas {@code} solamente para nombrar una clase o método, usa {@link} (ejemplo: en vez de {@code Objects} usa {@link
  Objects}, en vez de {@code Objects.equals(Object, Object)} usa {@link Objects#equals(Object, Object)})
- cuando uses {@link} para referenciar otras clases, métodos o campos públicos, no lo hagas con el fully qualified, solo
  coloca el nombre simple y luego agergaremos los imports necesarios.
- limita cada línea de comentario javadoc a un total de 120 caracteres contando espacios de indentación, y divide la
  línea si necesitas más espacio.
- usa <strong></strong> en lugar de <b></b>
- si necesitas escribir una lista, usa <ul> y <li>
- si necesitas escribir una enumeración usa <ol> y <li>
- si un item en <li> excederá la regla de 120 caracteres, usa <li> \n (ítem) \n </li>
- cuando documentes una clase de pruebas que contiene métodos anotados con @Test, a nivel de clase agrega al final del
  texto, las siguientes líneas:
``` html
<div style="border: 1px solid black; padding: 1px;">
<b>Execution note:</b> dvidal@infoyupay.com passed {} tests in {}s at {}
</div>
```
En las líneas a agregar en las clases de prueba deberás reemplazar los {} con la siguiente información que debes
obtener del prompt:
1. Cantidad de tests pasados.
2. Tiempo de ejecución de los tests en segundos.
3. Fecha y hora de ejecución hasta los minutos para UTC-5 en formato "YYYY-MM-dd HH:mm:ss UTC-5"