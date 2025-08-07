// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.enums;

/**
 * Representa os possíveis estados de um pedido no sistema.
 */
public enum EstadoPedido {
    PENDENTE,      // Pedido recém-criado, aguardando aprovação do gerente.
    APROVADO,      // Pedido aprovado, estoque debitado e valores contabilizados.
    CANCELADO,     // Pedido cancelado.
    ALTERACAO_SOLICITADA, // Vendedor solicitou uma alteração nos itens ou entrega.
    CANCELAMENTO_SOLICITADO // Vendedor solicitou o cancelamento do pedido.
}
