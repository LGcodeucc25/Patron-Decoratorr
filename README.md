# Museum Experience Pass

A Java 17 museum pass customization application.

## Features

- General museum admission
- Audio Guide
- Virtual Tour
- VIP Access
- Accessibility Mode
- Dynamic price calculation
- Responsive interface
- Java backend with built-in HTTP server

## Run

This project does not require Maven.

Open PowerShell in the project folder and run:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

Then:

```powershell
java -cp out com.museum.Main
```

Open:

http://localhost:8080

## Academic structure

The Java implementation uses object-oriented composition to build a customizable museum pass. The base pass and optional services are represented by separate classes, allowing combinations without modifying the base pass.

## Creational patterns added

The Decorator pattern solves *how to add services to a pass*, but it says nothing about
*how the pass is created in the first place*. Three creational patterns were added on top of
it, each one covering a different question.

Available option ids: `audio`, `virtual`, `vip`, `accessibility`.

### Prototype — `com.museum.prototype`

Answers: *how do we reuse a configuration that already exists?*

- **`PassPreset`** implements `Cloneable` and holds a preset name together with its list of
  option ids. Its `copy()` method returns a **deep copy**: the new preset is created with a
  `new ArrayList<>` of the options, so the clone and the original never share the same list.
  Without that deep copy, a client that added an option to its clone would silently corrupt
  the stored preset for everybody else.
- **`PresetRegistry`** is a final class with a static registry of prototypes.
  `getClone(String name)` returns a fresh deep copy of the requested preset and throws
  `IllegalArgumentException` when the name is unknown; `getPresetNames()` lists the
  available presets.

Registered presets:

| Preset | Options |
| --- | --- |
| `explorer` | `audio`, `virtual` |
| `premium` | `audio`, `vip` |
| `inclusive` | `audio`, `accessibility` |

### Builder — `com.museum.builder`

Answers: *how do we assemble a decorated pass in a readable way?*

- **`MuseumPassBuilder`** is the builder. It is created with the base pass to decorate
  (a `null` base raises `IllegalArgumentException`) and exposes one fluent build step per
  service: `withAudioGuide()`, `withVirtualTour()`, `withVipAccess()`,
  `withAccessibility()`, plus the generic `withOption(String id)` for ids that are only
  known at runtime. Every step returns the builder itself, an unknown id raises
  `IllegalArgumentException` and repeating an id is simply ignored, so a pass is never
  wrapped twice by the same decorator. Nothing is created until `build()` is called, which
  wraps the base with the matching decorators **in the order the options were added**.
- **`PassDirector`** is the director. It stores the recipes the museum sells most often so
  clients do not repeat the same chains: `buildFullExperience(base)` applies the four
  services, and `buildCulturalVisit(base)` applies `audio` and `virtual`.

```java
MuseumPass pass = new MuseumPassBuilder(new BasicMuseumPass())
        .withAudioGuide()
        .withVipAccess()
        .build();

MuseumPass everything = PassDirector.buildFullExperience(new BasicMuseumPass());
```

### Factory Method — `com.museum.factory`

Answers: *which kind of pass do we start from?*

- **`PassCreator`** is the abstract creator and declares the factory method
  `public abstract MuseumPass createBasePass()`. Subclasses decide which concrete pass is
  returned, so the rest of the application depends only on the `MuseumPass` interface.
- **`StandardPassCreator`** returns a `BasicMuseumPass`.
- **`StudentPassCreator`** returns a `StudentMuseumPass`.
- **`StudentMuseumPass`** is a new concrete product: "Student Museum Pass", price 9.00,
  services `["Museum Entry", "Student Discount"]`, and `activate()` reports
  `"Student museum pass activated successfully."`.

Adding a new audience later (for example a senior pass) only means adding a product and a
creator, without touching the builder, the presets or the decorators.

### How the four patterns work together

The patterns are chained, each one handing its result to the next:

```
Factory Method  ->  Prototype      ->  Builder            ->  Decorator
creates the         supplies the       adds the chosen        wraps the base,
base pass           option list        options in order       service by service
```

1. **Factory Method** produces the base pass for the requested audience.
2. **Prototype** optionally supplies the option list, as a deep copy of a stored preset.
3. **Builder** receives that base and those options and assembles the result.
4. **Decorator** does the actual work, since each option becomes a decorator wrapping the
   previous pass.

## API

### `GET /api/pass`

| Parameter | Values | Description |
| --- | --- | --- |
| `type` | `standard` \| `student` | Chooses the `PassCreator` that builds the base pass. Defaults to `standard`; an unknown value returns **400**. |
| `preset` | `explorer` \| `premium` \| `inclusive` | Applies the cloned options of a stored preset. When present it takes precedence over `options`. |
| `options` | comma separated ids | Explicit options: `audio`, `virtual`, `vip`, `accessibility`. Used when no `preset` is given. |

The selected options are applied through `MuseumPassBuilder.withOption(...)` followed by
`build()`, and the JSON response now also includes a `"type"` field with the requested pass
type.

Examples:

```
GET /api/pass?type=student&preset=explorer
GET /api/pass?type=standard&options=audio,vip
```

### `GET /api/presets`

Returns the names of the available presets:

```json
["explorer", "premium", "inclusive"]
```

## Team

| Member | Pattern |
| --- | --- |
| Luis Jamioy Guerrero | Builder |
| David Campiño | Prototype |
| Sara Valentina Delgado | Factory Method |
