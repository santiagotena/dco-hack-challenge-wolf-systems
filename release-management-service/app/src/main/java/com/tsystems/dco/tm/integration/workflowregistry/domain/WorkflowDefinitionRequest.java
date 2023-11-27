package com.tsystems.dco.tm.integration.workflowregistry.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class WorkflowDefinitionRequest {
  private String type;
  private String namespace;
  private Definition definition;
  private String description;
  private String created;
  private String status;
}
