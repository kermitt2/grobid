/**
 * Copyright 2010 INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.grobid.service.exceptions;

import org.grobid.core.exceptions.GrobidException;

public class GrobidServiceException extends GrobidException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -756089338090769910L;

	public GrobidServiceException() {
		super();
	}
	
	public GrobidServiceException(String msg) {
		super(msg);
	}
	
	public GrobidServiceException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
