package com.iftm.client.tests.services;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;

@ExtendWith(SpringExtension.class)
public class ClientServiceTests {
	
	@InjectMocks
	private ClientService service;
	
	@Mock
	private ClientRepository repository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private Client client;
	private ClientDTO clientDTO;
	private PageRequest pageRequest;
	private PageImpl<Client> page;
	private Double income;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 4L;
		clientDTO = ClientFactory.createClientDTO();
		client = ClientFactory.createClient();
		income = 4000.0;
		pageRequest = PageRequest.of(0, 6);
		page = new PageImpl<>(List.of(client));
		
		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
		Mockito.doThrow(ResourceNotFoundException.class).when(repository).getOne(nonExistingId);
		
		Mockito.when(repository.findAll(pageRequest))
			.thenReturn(page);
		
		Mockito.when(repository.findByIncome(ArgumentMatchers.anyDouble(), ArgumentMatchers.any()))
			.thenReturn(page);
		
		Mockito.when(repository.findById(existingId))
			.thenReturn(Optional.of(client));
		
		Mockito.when(repository.findById(nonExistingId))
			.thenReturn(Optional.empty());
		
		Mockito.when(repository.getOne(existingId))
			.thenReturn(clientDTO.toEntity());
		
		Mockito.when(repository.save(clientDTO.toEntity()))
			.thenReturn(clientDTO.toEntity());
		
		
	}
	
	/*Delete deveria  retornar vazio quando o id existir*/
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
	}
	
	/*Delete deveria  lançar uma EmptyResultDataAccessException quando o id não existir */
	@Test
	public void deleteShouldThrowEmptyResultDataAccessExceptionWhenIdDoesNotExists() {
		
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			service.delete(nonExistingId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);
	}
	
	/*Delete deveria lançar DataIntegrityViolationException quando a deleção implicar em uma
	  restrição de integridade.*/
	
	@Test
	public void deleteShouldThrowDataIntegrityViolationExceptionWhenIdHasDependecyIntegrity() {
		
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			service.delete(dependentId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
	}
	
	/* findAllPaged deveria retornar uma página (e chamar o método findAll do repository) */
	
	@Test
	public void findAllPagedShouldReturnPage() {		
		
		Page<ClientDTO> result = service.findAllPaged(pageRequest);
		
		Assertions.assertNotNull(result);
		Assertions.assertFalse(result.isEmpty());
		
		Mockito.verify(repository, Mockito.times(1)).findAll(pageRequest);
	}
	
	/*findByIncome deveria retornar uma página (e chamar o método findByIncome do
	  repository)*/
	
	@Test
	public void findByIncomeShouldReturnPage() {
		
		Page<ClientDTO> result = service.findByIncome(income, pageRequest);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(repository, Mockito.times(1)).findByIncome(income, pageRequest);		
	}
	
	/*findById deveria retornar um ClientDTO quando o id existir */
	
	@Test
	public void findByIdShouldReturnClientDTOWhenIdExists() {
		
		ClientDTO result = service.findById(existingId);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(repository, Mockito.times(1)).findById(existingId);
	}
	
	/*findById deveria lançar ResourceNotFoundException quando o id não existir*/
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).findById(nonExistingId);
	}
	
	/*Update deveria retornar um ClientDTO quando o id existir*/
	@Test
	public void updateShouldReturnClientDTOWhenIdExists() {
		
		ClientDTO result = service.update(existingId, clientDTO);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(repository, Mockito.times(1)).getOne(existingId);
	}
	
	/*Update deveria lançar uma ResourceNotFoundException quando o id não existir*/
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, clientDTO);
		});
		
		Mockito.verify(repository, Mockito.times(1)).getOne(nonExistingId);
	}
	
	/* Insert deveria retornar um ClientDTO ao inserir um novo cliente */
	@Test
	public void insertShouldReturnClientDTOWhenInsertNewClient() {
		
		ClientDTO result = service.insert(clientDTO);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(repository, Mockito.times(1)).save(clientDTO.toEntity());
	}

}
