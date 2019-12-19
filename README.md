(Note: this project has been renamed from [play-json-variants](https://github.com/julienrf/play-json-variants/tree/v2.0) to `play-json-derived-codecs`)

# Play JSON Derived Codecs [![](https://index.scala-lang.org/julienrf/play-json-derived-codecs/play-json-derived-codecs/latest.svg)](https://index.scala-lang.org/julienrf/play-json-derived-codecs)

`Reads`, `OWrites` and `OFormat` derivation for algebraic data types (sealed traits and case classes, possibly recursive), powered by [shapeless](http://github.com/milessabin/shapeless).

Compared to the built-in macros, this project brings support for:

- sealed traits ;
- recursive types ;
- polymorphic types.

The artifacts are built for Scala and Scala.js 2.12, and 2.13, Play 2.8 and Shapeless 2.3.

For Play 2.7 compatibility see version [`6.0.0`](https://github.com/julienrf/play-json-derived-codecs/tree/v6.0.0).

## Usage

~~~ scala
import julienrf.json.derived

case class User(name: String, age: Int)

object User {
  implicit val reads: Reads[User] = derived.reads
}
~~~

The [API](https://www.javadoc.io/doc/org.julienrf/play-json-derived-codecs_2.12) is simple: the object
`julienrf.json.derived` has just three methods.

- `reads[A]`, derives a `Reads[A]` ;
- `owrites[A]`, derives a `OWrites[A]` ;
- `oformat[A]`, derives a `OFormat[A]`.

### Representation of Sum Types

By default, sum types (types extending a sealed trait) are represented by a JSON object containing
one field whose name is the name of the concrete type and whose value is the JSON object containing
the value of the given type.

For instance, consider the following data type:

~~~ scala
sealed trait Foo
case class Bar(s: String, i: Int) extends Foo
case object Baz extends Foo
~~~

The default JSON representation of `Bar("quux", 42)` is the following JSON object:

~~~ javascript
{
  "Bar": {
    "s": "quux",
    "i": 42
  }
}
~~~

### Configuring the Derivation Process

Three aspects of the derivation process can be configured:

- the representation of sum types,
- the way case class field names are mapped to JSON property names,
- the type name used to discriminate sum types.

#### Custom Representation of Sum Types

The default representation of sum types may not fit all use cases. For instance, it is not very
practical for enumerations.

For instance, you might want to represent the `Bar("quux", 42)` value as the following JSON object:

~~~ javascript
{
  "type": "Bar",
  "s": "quux",
  "i": 42
}
~~~

Here, the type information is flattened with the `Bar` members.

You can do so by using the methods in the `derived.flat` object:

~~~ scala
implicit val fooOWrites: OWrites[Foo] =
  derived.flat.owrites((__ \ "type").write[String])
~~~

In case you need even more control, you can implement your own `TypeTagOWrites` and `TypeTagReads`.

#### Custom Field Names Mapping

By default, case class fields are mapped to JSON object properties having the same name.

You can transform this mapping by supplying a different `NameAdapter` parameter. For
instance, to use “snake case” in JSON:

~~~ scala
implicit val userFormat: OFormat[User] = derived.oformat(adapter = NameAdapter.snakeCase)
~~~

#### Custom Type Names

By default, case class names are used as discriminators (type tags) for sum types.

You can configure the type tags to use by using the `derived.withTypeTag` object:

~~~ scala
implicit val fooFormat: OFormat[Foo] =
  derived.withTypeTag.oformat(TypeTagSetting.FullClassName)
~~~

The library provides the following `TypeTagSetting` values out of the box:

- `TypeTagSetting.ShortClassName`: use the class name (as it is defined in Scala)
- `TypeTagSetting.FullClassName`: use the fully qualified name
- `TypeTagSetting.UserDefinedName`: require the presence of an implicit `CustomTypeTag[A]`
  for all type `A` of the sum type, providing the type tag to use

### Custom format for certain types in hierarchy

Sometimes, you might want to represent one type differently than default format would. This can be done by creating an implicit instance of `DerivedReads` or `DerivedWrites` for said type. Below is an example of implementing both custom reads and writes for a single class in a hierarchy:

~~~ scala
sealed trait Hierarchy
case class First(x: Integer)
case class Second(y: Integer)

implicit val SecondReads: DerivedReads[Second] = new DerivedReads[Second] {
  def reads(tagReads: TypeTagReads, adapter: NameAdapter) = tagReads.reads("Second", (__ \ "foo").read[Integer].map(foo => Second(foo)))
}

implicit val SecondWrites: DerivedOWrites[Second] = new DerivedOWrites[Second] {
  override def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter): OWrites[Second] =
    tagOwrites.owrites[Second](
      "Second",
      OWrites[Second](s => JsObject(("foo", Json.toJson(s.y)) :: Nil))
    )
}

val defaultTypeFormat = (__ \ "type").format[String]
implicit val HierarchyFormat = derived.flat.oformat[Hierarchy](defaultTypeFormat)
~~~

This will cause `Second` to be read with `SecondReads`, and read with `SecondWrites`.

## Contributors

See [here](https://github.com/julienrf/play-json-variants/graphs/contributors).

## Changelog

See [here](https://github.com/julienrf/play-json-derived-codecs/releases).
