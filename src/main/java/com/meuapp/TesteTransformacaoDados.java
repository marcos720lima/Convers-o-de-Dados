package com.meuapp;

import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.*;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TesteTransformacaoDados {
    private static final String URL_PDF = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos/Anexo_I_Rol_2021RN_465.2021_RN627L.2024.pdf";
    private static final String NOME_ARQUIVO_CSV = "tabela_procedimentos.csv";
    private static final String NOME_ARQUIVO_ZIP = "Teste_Marcos.zip";

    public static void main(String[] args) {
        try {
            List<List<String>> dados = extrairDadosDoPDF();
            salvarEmCSV(dados);
            compactarCSV();
            substituirAbreviacoes(dados);
            new File(NOME_ARQUIVO_CSV).delete();
            System.out.println("Processamento concluído com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro durante o processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<List<String>> extrairDadosDoPDF() throws IOException {
        List<List<String>> dadosExtraidos = new ArrayList<>();
        List<String> cabecalho = null;

        File pdfTemp = File.createTempFile("temp", ".pdf");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(URL_PDF);
            try (FileOutputStream fos = new FileOutputStream(pdfTemp)) {
                client.execute(request, response -> {
                    response.getEntity().writeTo(fos);
                    return null;
                });
            }
        }

        try (FileInputStream fis = new FileInputStream(pdfTemp);
                PDDocument document = PDDocument.load(fis);
                ObjectExtractor extractor = new ObjectExtractor(document)) {

            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

            for (int pagina = 1; pagina <= document.getNumberOfPages(); pagina++) {
                Page paginaPDF = extractor.extract(pagina);
                List<Table> tabelas = sea.extract(paginaPDF);

                for (Table tabela : tabelas) {
                    @SuppressWarnings("unchecked")
                    List<List<RectangularTextContainer<TextElement>>> linhas = (List<List<RectangularTextContainer<TextElement>>>) (List<?>) tabela
                            .getRows();

                    if (cabecalho == null && !linhas.isEmpty()) {
                        cabecalho = new ArrayList<>();
                        for (RectangularTextContainer<TextElement> celula : linhas.get(0)) {
                            cabecalho.add(celula.getText().trim());
                        }
                        dadosExtraidos.add(cabecalho);
                    }

                    for (int i = cabecalho == null ? 0 : 1; i < linhas.size(); i++) {
                        List<String> linhaProcessada = new ArrayList<>();
                        for (RectangularTextContainer<TextElement> celula : linhas.get(i)) {
                            linhaProcessada.add(celula.getText().trim());
                        }
                        while (linhaProcessada.size() < cabecalho.size()) {
                            linhaProcessada.add("");
                        }
                        dadosExtraidos.add(linhaProcessada);
                    }
                }
            }
        }

        pdfTemp.delete();
        return dadosExtraidos;
    }

    private static void salvarEmCSV(List<List<String>> dados) throws IOException {
        try (FileWriter fileWriter = new FileWriter(NOME_ARQUIVO_CSV);
                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {

            for (List<String> linha : dados) {
                csvPrinter.printRecord(linha);
            }
            csvPrinter.flush();
        }
    }

    private static void compactarCSV() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(NOME_ARQUIVO_ZIP);
                ZipOutputStream zos = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(NOME_ARQUIVO_CSV)) {

            ZipEntry zipEntry = new ZipEntry(NOME_ARQUIVO_CSV);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }

    private static void substituirAbreviacoes(List<List<String>> dados) {
        if (dados.isEmpty())
            return;

        Map<String, String> mapeamento = new HashMap<>();
        mapeamento.put("OD", "Segmentação Odontológica");
        mapeamento.put("AMB", "Segmentação Ambulatorial");

        List<String> cabecalho = dados.get(0);
        int indexOD = cabecalho.indexOf("OD");
        int indexAMB = cabecalho.indexOf("AMB");

        for (int i = 1; i < dados.size(); i++) {
            List<String> linha = dados.get(i);
            if (indexOD >= 0 && indexOD < linha.size()) {
                String valorOD = linha.get(indexOD);
                linha.set(indexOD, mapeamento.getOrDefault(valorOD, valorOD));
            }
            if (indexAMB >= 0 && indexAMB < linha.size()) {
                String valorAMB = linha.get(indexAMB);
                linha.set(indexAMB, mapeamento.getOrDefault(valorAMB, valorAMB));
            }
        }
    }
}
