package coretech.sistemaCoreTech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import coretech.sistemaCoreTech.model.Pedido;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // pedidos de um usuario - usado ao remover um perfil ADMIN antigo do banco
    List<Pedido> findByUsuarioId(Long id);
}
