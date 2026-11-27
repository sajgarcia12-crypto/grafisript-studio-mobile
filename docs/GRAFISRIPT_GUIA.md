# El Grafisript — sintaxis fácil

Grafisript acepta tanto comandos clásicos como una sintaxis declarativa en español. Las cabeceras terminan en dos puntos y las propiedades se escriben debajo con `nombre = valor`.

## Ejemplo básico

```grafisript
Crear_mundo:
    fondo_color = negro_con_rojo

Crear_personaje:
    vida = 100
    velocidad = 5

Enemigo:
    daño = 20

Ese_(enemigo):
    quitar_vida_al_jugador = 20
```

El intérprete reconoce estas cabeceras:
- `Crear_mundo:` — inicia un mundo y permite configurar su fondo.
- `Crear_personaje:` o `Personaje:` — crea el jugador y permite configurar vida y velocidad.
- `Enemigo:` — configura el enemigo y su daño.
- `Ese_(enemigo):` — indica que las acciones siguientes se aplican al enemigo configurado.

Colores de fondo admitidos: `negro`, `rojo`, `negro_con_rojo`, `azul`, `verde`, `blanco` y hexadecimal `#RRGGBB`.

## Sintaxis clásica que sigue disponible

```grafisript
variable monedas = 5
crear_personaje Jugador
jugador.vida(100)
jugador.mover(2, 1)

si monedas > 3 {
    mostrar_mensaje "¡Ganaste!"
} sino {
    mostrar_mensaje "Sigue intentando"
}

repetir 3 {
    crear_bloque piedra 1 0 1
}

funcion saludar {
    mostrar_mensaje "Hola"
}
llamar saludar
```

## Límites actuales

Esta es una primera ampliación funcional del intérprete integrado en Android, no todavía un compilador general de propósito general. La sintaxis declarativa descrita arriba actualiza propiedades del proyecto y ejecuta el daño sobre la vida del jugador. Las funciones avanzadas (tipos complejos, clases, módulos, depurador paso a paso y sistema completo de física programable) requieren implementaciones posteriores.

## Memoria

La opción de memoria de BlockForge es un presupuesto orientativo para cachés y recursos temporales. Android controla la RAM real del proceso y no ofrece un permiso normal que permita a una app fijar arbitrariamente cuánta RAM puede usar. No se fuerza un heap grande, para evitar perjudicar dispositivos con memoria limitada.
