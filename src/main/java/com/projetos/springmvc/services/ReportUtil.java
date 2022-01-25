package com.projetos.springmvc.services;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReportUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	// Retorna nosso Relatorio em BYTE para download no navegador
	public byte[] gerarRelatorio(List dados, String relatorio, ServletContext context) throws Exception {

		// Cria a lista de dados para o relatório.
		JRBeanCollectionDataSource jrbean = new JRBeanCollectionDataSource(dados);

		// Carregar o caminho do arquivo Jasper Compilado.
		String caminhoJasper = context.getRealPath("relatorios") + File.separator + relatorio + ".jasper";

		// Carrega o arquivo Jasper, atribuindo os dados.
		JasperPrint impressora = JasperFillManager.fillReport(caminhoJasper, new HashMap(), jrbean);

		return JasperExportManager.exportReportToPdf(impressora);
	}
	
	
}
