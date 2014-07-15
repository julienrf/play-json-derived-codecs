# Play! JSON Variants

This artifact provides a function `Variants.format[A]` that takes as parameter a root type hierarchy `A` and generates a Play! `Format[A]` JSON serializer/deserializer that supports all the subtypes of `A`.

For instance, consider the following class hierarchy:

```scala
sealed trait Foo
case class Bar(x: Int) extends Foo
case class Baz(s: String) extends Foo
case class Bah(s: String) extends Foo
```

How to write a `Reads[Foo]` JSON deserializer able to build the right variant of `Foo` given a JSON value? The naive approach could be to write the following:

```scala
import play.api.libs.json._
import play.api.libs.functional.syntax._

implicit val fooReads: Reads[Foo] = (__ \ "x").read[Int].map[Foo](Bar) |
                                    (__ \ "s").read[String].map[Foo](Baz) |
                                    (__ \ "s").read[String].map[Foo](Bah)
```

However this wouldn’t work because the deserializer is unable to distinguish between `Baz` and `Bah` values:

```scala
val json = Json.obj("s" -> "hello")
val foo = json.validate[Foo] // Is it a `Baz` or a `Bah`?
println(foo) // "Success(Baz(hello))"
```

Any JSON value containing a `String` field `x` is always considered to be a `Baz` value by the deserializer (though it could be a `Bah`), just because the `Baz` and `Bah` deserializers are tried in order.

In order to differentiate between all the `Foo` variants, we need to add a field in the JSON representation of `Foo` values:

```scala
val bahJson = Json.obj("s" -> "hello", "$variant" -> "Bah") // This is a `Bah`
val bazJson = Json.obj("s" -> "bye", "$variant" -> "Baz") // This is a `Baz`
val barJson = Json.obj("x" -> "42", "$variant" -> "Bar") // And this is a `Bar`
```

The deserializer can then be written as follows:

```scala
implicit val fooReads: Reads[Foo] = (__ \ "$variant").read[String].flatMap[Foo] {
  case "Bar" => (__ \ "x").read[Int].map(Bar)
  case "Baz" => (__ \ "s").read[String].map(Baz)
  case "Bah" => (__ \ "s").read[String].map(Bah)
}
```

Usage:

```scala
bahJson.validate[Foo] // Success(Bah("hello"))
bazJson.validate[Foo] // Success(Baz("bye"))
```

The above text introduced a problem and its solution, but this one is very cumbersome: you don’t want to always write by hand the JSON serializer and deserializer of your data type hierarchy.

The purpose of this project is to generate these serializer and deserializer for you. Just write the following and you are done:

```scala
import julienrf.variants.Variants

implicit val format: Format[Foo] = Variants.format[Foo]
```

By default the field used to discriminate the target object’s type is named `$variant` but you can supply another name:

```scala
implicit val format: Format[Foo] = Variants.format[Foo]("type")
```

# Installation

Add the following dependency to your project:

```scala
libraryDependencies += "org.julienrf" %% "play-json-variants" % "0.2"
```

The 0.2 version is compatible with Play 2.3.x and with both Scala 2.10 and 2.11.

# How Does It Work?

The `Variants.format[Foo]` is a Scala macro that takes as parameter the root type of a class hierarchy and expands to code equivalent to the hand-written version: it adds a `$variant` field to the default JSON serializer, containing the name of the variant, and uses it to deserialize values to the correct type.

# Known Limitations

* For now the macro expects its type parameter to be the root **sealed trait** of a class hierarchy made of **case classes** or **case objects** ;
* Recursive types are not supported ;
* Polymorphic types are not supported.
