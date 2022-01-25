package com.projetos.springmvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;

import com.projetos.springmvc.model.Pessoa;
import com.projetos.springmvc.model.Telefone;
import com.projetos.springmvc.repository.PessoaRepository;
import com.projetos.springmvc.repository.ProfissaoRepository;
import com.projetos.springmvc.repository.TelefoneRepository;
import com.projetos.springmvc.services.ReportUtil;

@Controller

public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private ReportUtil reportUtil;

	@Autowired
	private ProfissaoRepository profissaoRepository;

	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}

	@GetMapping("/pessoaspag")
	public ModelAndView carregaPaginacao(
			@PageableDefault(size = 2, sort = { "id" }) org.springframework.data.domain.Pageable pageable,
			ModelAndView model, @RequestParam("nomepesquisa") String nomepesquisa) {
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.addObject("profissoes", profissaoRepository.findAll());
		model.setViewName("cadastro/cadastropessoa");
		return model;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 2, Sort.by("id"))));
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = { "multipart/form-data" })
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file)
			throws IOException {

		if (bindingResult.hasErrors()) {
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 2, Sort.by("id"))));
			modelAndView.addObject("pessoaobj", pessoa);
			modelAndView.addObject("profissoes", profissaoRepository.findAll());
			List<String> msg = new ArrayList<String>();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage()); // vem das anotações @NotEmpty e outras
			}

			modelAndView.addObject("msg", msg);
			return modelAndView;
		}

		if (file.getSize() > 0) {
			pessoa.setFile(file.getBytes());
			pessoa.setFileName(file.getOriginalFilename());
			pessoa.setFileType(file.getContentType());
		} else {
			if (pessoa.getId() != null & pessoa.getId() > 0) {
				Pessoa temporario = pessoaRepository.findById(pessoa.getId()).get();
				pessoa.setFile(temporario.getFile());
				pessoa.setFileName(temporario.getFileName());
				pessoa.setFileType(temporario.getFileType());
			}
		}

		pessoaRepository.save(pessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 2, Sort.by("id"))));
		andView.addObject("pessoaobj", new Pessoa());

		return andView;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 2, Sort.by("id"))));
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}

	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa, @PageableDefault(size = 2, sort = { "id" }) Pageable pageable) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		Page<Pessoa> pessoas = pessoaRepository.findPessoaByNamePage("", pageable);
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;

	}

	@GetMapping("/downloadFile/{idpessoa}")
	public void downloadFile(@PathVariable("idpessoa") Long idpessoa, HttpServletResponse response) throws IOException {

		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();

		if (pessoa.getFile() != null) {

			response.setContentLength(pessoa.getFile().length);
			response.setContentType(pessoa.getFileType());

			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filenname=\"%s\"", pessoa.getFileName());
			response.setHeader(headerKey, headerValue);

			response.getOutputStream().write(pessoa.getFile());

		}

	}

	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {

		pessoaRepository.deleteById(idpessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 2, Sort.by("id"))));
		modelAndView.addObject("pessoaobj", new Pessoa());
		return modelAndView;

	}

	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("sexopesquisa") String sexopesquisa,
			@PageableDefault(size = 2, sort = { "id" }) Pageable pageable) {

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		Page<Pessoa> pessoas = null;

		if (sexopesquisa != null && !sexopesquisa.isEmpty()) {
			 pessoas = pessoaRepository.findBySexPage(sexopesquisa, pageable);
		} else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}

		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("nomepesquisa", nomepesquisa);
		return modelAndView;
	}

	@GetMapping("**/pesquisarpessoa")
	public void gerarPDF(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("sexopesquisa") String sexopesquisa, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// Preparaçao da lista de dados para o ReportUtil
		List<Pessoa> dados = new ArrayList<Pessoa>();

		if (sexopesquisa != null && !sexopesquisa.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) {
			dados = pessoaRepository.findBySex(nomepesquisa, sexopesquisa);
		} else if (!nomepesquisa.isEmpty() && nomepesquisa != null) {
			dados = pessoaRepository.findPessoaByName(nomepesquisa);
		} else {
			dados = (List<Pessoa>) pessoaRepository.findAll();
		}

		// Chamando serviço de geraçao de relatorios
		byte[] pdf = reportUtil.gerarRelatorio(dados, "pessoa", request.getServletContext());

		// Setando o tamanho da resposta
		response.setContentLength(pdf.length);

		// Setando tipo da resposta
		response.setContentType("application/octet-stream");

		// Definir o cabecalho da resposta
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);

		// Finaliza a resposta
		response.getOutputStream().write(pdf);

	}

	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("msg", new ArrayList<String>());
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return modelAndView;

	}

	@PostMapping("**/addfonePessoa/{pessoaid}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {

		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();

		if (telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {

			ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
			modelAndView.addObject("pessoaobj", pessoa);
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));

			List<String> msg = new ArrayList<String>();
			if (telefone.getNumero().isEmpty()) {
				msg.add("Numero deve ser informado");
			}

			if (telefone.getTipo().isEmpty()) {
				msg.add("Tipo deve ser informado");
			}
			modelAndView.addObject("msg", msg);

			return modelAndView;

		}

		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");

		telefone.setPessoa(pessoa);

		telefoneRepository.save(telefone);

		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		return modelAndView;
	}

	@GetMapping("/removertelefone/{idtelefone}")
	public ModelAndView removertelefone(@PathVariable("idtelefone") Long idtelefone) {

		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();

		telefoneRepository.deleteById(idtelefone);

		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		return modelAndView;

	}

}
