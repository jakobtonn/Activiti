/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.editor.language.json.converter;

import java.util.Map;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.SequenceFlow;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class SequenceFlowJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    
    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }
  
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_SEQUENCE_FLOW, SequenceFlowJsonConverter.class);
  }
  
  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(SequenceFlow.class, SequenceFlowJsonConverter.class);
  }
  
  @Override
  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_SEQUENCE_FLOW;
  }
  
  @Override
  public void convertToJson(BaseElement baseElement, ActivityProcessor processor,
      BpmnModel model, FlowElementsContainer container, ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {
    
    SequenceFlow sequenceFlow = (SequenceFlow) baseElement;
    ObjectNode flowNode = BpmnJsonConverterUtil.createChildShape(sequenceFlow.getId(), STENCIL_SEQUENCE_FLOW, 172, 212, 128, 212);
    ArrayNode dockersArrayNode = objectMapper.createArrayNode();
    ObjectNode dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getSourceRef()).getWidth() / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getSourceRef()).getHeight() / 2.0);
    dockersArrayNode.add(dockNode);
    
    if (model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() > 2) {
      for (int i = 1; i < model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() - 1; i++) {
        GraphicInfo graphicInfo =  model.getFlowLocationGraphicInfo(sequenceFlow.getId()).get(i);
        dockNode = objectMapper.createObjectNode();
        dockNode.put(EDITOR_BOUNDS_X, graphicInfo.getX());
        dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.getY());
        dockersArrayNode.add(dockNode);
      }
    }
    
    dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getTargetRef()).getWidth() / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getTargetRef()).getHeight() / 2.0);
    dockersArrayNode.add(dockNode);
    flowNode.put("dockers", dockersArrayNode);
    ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
    outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
    flowNode.put("outgoing", outgoingArrayNode);
    flowNode.put("target", BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
    
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    propertiesNode.put(PROPERTY_OVERRIDE_ID, sequenceFlow.getId());
    if (StringUtils.isNotEmpty(sequenceFlow.getName())) {
        propertiesNode.put(PROPERTY_NAME, sequenceFlow.getName());
    }
    
    if (StringUtils.isNotEmpty(sequenceFlow.getDocumentation())) {
        propertiesNode.put(PROPERTY_DOCUMENTATION, sequenceFlow.getDocumentation());
    }
    
    if (sequenceFlow.getExtensionElements().get("conditionFieldId") != null) {
        String conditionFieldId = sequenceFlow.getExtensionElements().get("conditionFieldId").get(0).getElementText();
        
        String conditionOperator = null;
        if (sequenceFlow.getExtensionElements().get("conditionOperator") != null) {
            conditionOperator = sequenceFlow.getExtensionElements().get("conditionOperator").get(0).getElementText();
        }
        
        String conditionValue = null;
        if (sequenceFlow.getExtensionElements().get("conditionValue") != null) {
            conditionValue = sequenceFlow.getExtensionElements().get("conditionValue").get(0).getElementText();
        }
        
        if (StringUtils.isNotEmpty(conditionFieldId) && StringUtils.isNotEmpty(conditionOperator) && 
                StringUtils.isNotEmpty(conditionValue)) {
            
            ObjectNode expressionNode = objectMapper.createObjectNode();
            expressionNode.put("type", "variables");
            expressionNode.put("fieldType", "field");
            expressionNode.put("fieldId", conditionFieldId);
            expressionNode.put("operator", conditionOperator);
            expressionNode.put("value", conditionValue);
            ObjectNode conditionNode = objectMapper.createObjectNode();
            conditionNode.put("expression", expressionNode);
            propertiesNode.put(PROPERTY_SEQUENCEFLOW_CONDITION, conditionNode);
        }
        
    } else if (sequenceFlow.getExtensionElements().get("conditionFormId") != null) {
        String conditionFormId = sequenceFlow.getExtensionElements().get("conditionFormId").get(0).getElementText();
        
        String conditionOperator = null;
        if (sequenceFlow.getExtensionElements().get("conditionOperator") != null) {
            conditionOperator = sequenceFlow.getExtensionElements().get("conditionOperator").get(0).getElementText();
        }
        
        String conditionOutcomeName = null;
        if (sequenceFlow.getExtensionElements().get("conditionOutcomeName") != null) {
            conditionOutcomeName = sequenceFlow.getExtensionElements().get("conditionOutcomeName").get(0).getElementText();
        }
        
        if (StringUtils.isNotEmpty(conditionFormId) && StringUtils.isNotEmpty(conditionOperator) && 
                StringUtils.isNotEmpty(conditionOutcomeName)) {
            
            ObjectNode expressionNode = objectMapper.createObjectNode();
            expressionNode.put("type", "variables");
            expressionNode.put("fieldType", "outcome");
            expressionNode.put("outcomeFormId", conditionFormId);
            expressionNode.put("operator", conditionOperator);
            expressionNode.put("outcomeName", conditionOutcomeName);
            ObjectNode conditionNode = objectMapper.createObjectNode();
            conditionNode.put("expression", expressionNode);
            propertiesNode.put(PROPERTY_SEQUENCEFLOW_CONDITION, conditionNode);
        }
        
    } else if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
        ObjectNode expressionNode = objectMapper.createObjectNode();
        expressionNode.put("type", "static");
        expressionNode.put("staticValue", sequenceFlow.getConditionExpression());
        ObjectNode conditionNode = objectMapper.createObjectNode();
        conditionNode.put("expression", expressionNode);
        propertiesNode.put(PROPERTY_SEQUENCEFLOW_CONDITION, conditionNode);
    }
    
    FlowElement sourceElement = model.getFlowElement(sequenceFlow.getSourceRef());
    if (sourceElement != null) {
        
        String defaultFlow = null;
        if (sourceElement instanceof Gateway) {
            Gateway gateway = (Gateway) sourceElement;
            defaultFlow = gateway.getDefaultFlow();
            
        } else if (sourceElement instanceof Activity) {
            Activity activity = (Activity) sourceElement;
            defaultFlow = activity.getDefaultFlow();
        }
        
        if (StringUtils.isNotEmpty(defaultFlow) && defaultFlow.equals(sequenceFlow.getId())) {
            propertiesNode.put("defaultflow", true);
        }
    }
    
    flowNode.put(EDITOR_SHAPE_PROPERTIES, propertiesNode);
    shapesArrayNode.add(flowNode);
  }
  
  @Override
  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    // nothing to do
  }
  
  @Override
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    SequenceFlow flow = new SequenceFlow();
    
    String sourceRef = BpmnJsonConverterUtil.lookForSourceRef(elementNode.get(EDITOR_SHAPE_ID).asText(), modelNode.get(EDITOR_CHILD_SHAPES));
    if (sourceRef != null) {
      flow.setSourceRef(sourceRef);
      JsonNode targetNode = elementNode.get("target");
      if (targetNode != null && targetNode.isNull() == false) {
          String targetId = targetNode.get(EDITOR_SHAPE_ID).asText();
          if (shapeMap.get(targetId) != null) {
              flow.setTargetRef(BpmnJsonConverterUtil.getElementId(shapeMap.get(targetId)));
          }
      }
    }
    
    JsonNode conditionNode = getProperty(PROPERTY_SEQUENCEFLOW_CONDITION, elementNode);
    if (conditionNode != null) {
   
        if (conditionNode.isTextual() && conditionNode.isNull() == false) {
            flow.setConditionExpression(conditionNode.asText());
        
        } else if (conditionNode.get("expression") != null) {
            JsonNode expressionNode = conditionNode.get("expression");
            if (expressionNode.get("type") != null) {
                String expressionType = expressionNode.get("type").asText();
                
                if ("variables".equalsIgnoreCase(expressionType) && expressionNode.get("fieldType") != null) {
                
                    String fieldType = expressionNode.get("fieldType").asText();
                    
                    if ("field".equalsIgnoreCase(fieldType)) {
                        setFieldConditionExpression(flow, expressionNode);
                    
                    } else if ("outcome".equalsIgnoreCase(fieldType)) {
                        setOutcomeConditionExpression(flow, expressionNode);
                    }
                    
                } else if (expressionNode.get("staticValue") != null && expressionNode.get("staticValue").isNull() == false) {
                    flow.setConditionExpression(expressionNode.get("staticValue").asText());
                }
            }
        }
    }
    
    return flow;
  }
  
  protected void setFieldConditionExpression(SequenceFlow flow, JsonNode expressionNode) {
      String fieldId = null;
      if (expressionNode.get("fieldId") != null && expressionNode.get("fieldId").isNull() == false) {
          fieldId = expressionNode.get("fieldId").asText();
      }
      
      String operator = null;
      if (expressionNode.get("operator") != null && expressionNode.get("operator").isNull() == false) {
          operator = expressionNode.get("operator").asText();
      }
      
      String value = null;
      if (expressionNode.get("value") != null && expressionNode.get("value").isNull() == false) {
          value = expressionNode.get("value").asText();
      }
      
      if (fieldId != null && operator != null && value != null) {
          flow.setConditionExpression("${" + fieldId + " " + operator + " " + value + "}");
          addExtensionElement("conditionFieldId", fieldId, flow);
          addExtensionElement("conditionOperator", operator, flow);
          addExtensionElement("conditionValue", value, flow);
      }
  }
  
  protected void setOutcomeConditionExpression(SequenceFlow flow, JsonNode expressionNode) {
      Long formId = null;
      if (expressionNode.get("outcomeFormId") != null && expressionNode.get("outcomeFormId").isNull() == false) {
          formId = expressionNode.get("outcomeFormId").asLong();
      }
      
      String operator = null;
      if (expressionNode.get("operator") != null && expressionNode.get("operator").isNull() == false) {
          operator = expressionNode.get("operator").asText();
      }
      
      String outcomeName = null;
      if (expressionNode.get("outcomeName") != null && expressionNode.get("outcomeName").isNull() == false) {
          outcomeName = expressionNode.get("outcomeName").asText();
      }
      
      if (formId != null && operator != null && outcomeName != null) {
          flow.setConditionExpression("${form" + formId + "outcome " + operator + " " + outcomeName + "}");
          addExtensionElement("conditionFormId", String.valueOf(formId), flow);
          addExtensionElement("conditionOperator", operator, flow);
          addExtensionElement("conditionOutcomeName", outcomeName, flow);
      }
  }
  
  protected void addExtensionElement(String name, String value, SequenceFlow flow) {
      ExtensionElement extensionElement = new ExtensionElement();
      extensionElement.setNamespace(NAMESPACE);
      extensionElement.setNamespacePrefix("modeler");
      extensionElement.setName(name);
      extensionElement.setElementText(value);
      flow.addExtensionElement(extensionElement);
  }
}
