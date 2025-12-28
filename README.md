# PdFX - PDF Operations Desktop Application

Convert, Extract, Split, Merge, Encrypt, Decrypt and perform many other PDF operations.

## Features

- **Merge** multiple PDF files into one
- **Split** PDF files by page ranges
- **Extract** text and images from PDFs
- **Convert** PDF to images
- **Encrypt/Decrypt** PDF files with password protection
- **Compress** PDF files to reduce size
- **Edit Metadata** of PDF files

## Screenshots

![complprogress.png](screenshots%2Fcomplprogress.png)

![compress.png](screenshots%2Fcompress.png)

![convert.png](screenshots%2Fconvert.png)

![encrypt.png](screenshots%2Fencrypt.png)

![extract text.png](screenshots%2Fextract%20text.png)

![home.png](screenshots%2Fhome.png)

![in-progress.png](screenshots%2Fin-progress.png)

![meta.png](screenshots%2Fmeta.png)

![split.png](screenshots%2Fsplit.png)

## Libraries used

- Apache Commons IO
- Apache PDFBox
- Ikonli Fonts
- CSS Themes by AtlantaFX

## Requirements

For development:
- JDK 25 (Liberica JDK with JavaFX recommended)
- Maven 3.8+

For running the application:
- No external dependencies needed - the application is bundled with its own runtime

## Build

```bash
mvn clean install
```

The executable will be created at `ui/target/dist/PdFX/pdfx.exe`

## Run

### From Release
- Download and extract the zip from the [Releases](https://github.com/Vidhan31/pdfx/releases) page
- Run `pdfx.exe`

### From Source
```bash
mvn clean javafx:run
```

## License

[Apache License 2.0](LICENSE)
