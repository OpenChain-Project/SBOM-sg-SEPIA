/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultWebResponse {

	private String message;
	private String data;
	private String status;
}