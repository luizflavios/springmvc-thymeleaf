package com.projetos.springmvc.repository;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.projetos.springmvc.model.Pessoa;

@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

	@Query("select p from Pessoa p where p.nome like %?1% ")
	List<Pessoa> findPessoaByName(String nome);

	@Query("select p from Pessoa p where p.nome like %?1% and p.sexo = ?2")
	List<Pessoa> findBySex(String nome, String sexo);

	default Page<Pessoa> findPessoaByNamePage(String nome, Pageable pageable) {
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);

		// Configurando a pesquisa para funcionar apenas por partes == LIKE SQL
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().withMatcher("nome",
				ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		// Une o objeto com o parametro passado na pesquisa e a configuraçao realizada
		// acima.
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);

		Page<Pessoa> pessoas = findAll(example, pageable);

		return pessoas;
	}

	default Page<Pessoa> findBySexPage(String sexo, Pageable pageable) {
		Pessoa pessoa = new Pessoa();
		
		pessoa.setSexo(sexo);
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
																	.withMatcher("sexo", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		//Une o objeto com o parametro passado na pesquisa e a configuraçao realizada acima. 
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);

		Page<Pessoa> pessoas = findAll(example, pageable);

		return pessoas;
	}
	
	
}
