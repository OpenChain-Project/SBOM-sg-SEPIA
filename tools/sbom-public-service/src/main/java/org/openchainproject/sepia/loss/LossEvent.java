/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.loss;

import java.util.LinkedHashMap;
import java.util.Map;

public class LossEvent {
    private String eventId, severity, kind, sourceFormat, targetFormat, entityKey, sourcePath, targetPath, ruleId, reason, toolName, toolVersion;
    private Object sourceValue, targetValue;
    public String getEventId(){return eventId;} public void setEventId(String v){eventId=v;}
    public String getSeverity(){return severity;} public void setSeverity(String v){severity=v;}
    public String getKind(){return kind;} public void setKind(String v){kind=v;}
    public String getSourceFormat(){return sourceFormat;} public void setSourceFormat(String v){sourceFormat=v;}
    public String getTargetFormat(){return targetFormat;} public void setTargetFormat(String v){targetFormat=v;}
    public String getEntityKey(){return entityKey;} public void setEntityKey(String v){entityKey=v;}
    public String getSourcePath(){return sourcePath;} public void setSourcePath(String v){sourcePath=v;}
    public String getTargetPath(){return targetPath;} public void setTargetPath(String v){targetPath=v;}
    public Object getSourceValue(){return sourceValue;} public void setSourceValue(Object v){sourceValue=v;}
    public Object getTargetValue(){return targetValue;} public void setTargetValue(Object v){targetValue=v;}
    public String getRuleId(){return ruleId;} public void setRuleId(String v){ruleId=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public String getToolName(){return toolName;} public void setToolName(String v){toolName=v;}
    public String getToolVersion(){return toolVersion;} public void setToolVersion(String v){toolVersion=v;}
}
