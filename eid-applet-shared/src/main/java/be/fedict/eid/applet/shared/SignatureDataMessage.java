/*
 * eID Applet Project.
 * Copyright (C) 2008-2009 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.eid.applet.shared;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import be.fedict.eid.applet.shared.annotation.Description;
import be.fedict.eid.applet.shared.annotation.HttpBody;
import be.fedict.eid.applet.shared.annotation.HttpHeader;
import be.fedict.eid.applet.shared.annotation.MessageDiscriminator;
import be.fedict.eid.applet.shared.annotation.NotNull;
import be.fedict.eid.applet.shared.annotation.PostConstruct;
import be.fedict.eid.applet.shared.annotation.ProtocolStateAllowed;
import be.fedict.eid.applet.shared.annotation.ResponsesAllowed;
import be.fedict.eid.applet.shared.protocol.ProtocolState;

/**
 * Signature Data transfer object.
 * 
 * @author fcorneli
 * 
 */
@ResponsesAllowed(FinishedMessage.class)
@ProtocolStateAllowed(ProtocolState.SIGN)
public class SignatureDataMessage extends AbstractProtocolMessage {
	@HttpHeader(TYPE_HTTP_HEADER)
	@MessageDiscriminator
	public static final String TYPE = SignatureDataMessage.class
			.getSimpleName();

	@HttpHeader(HTTP_HEADER_PREFIX + "SignatureValueSize")
	@NotNull
	public Integer signatureValueSize;

	@HttpBody
	@NotNull
	@Description("Contains concatenation of signature value and sign cert chain.")
	public byte[] body;

	@PostConstruct
	public void postConstruct() {
		if (this.signatureValueSize != 128) {
			throw new RuntimeException("signature value size invalid");
		}
		this.signatureValue = Arrays.copyOfRange(this.body, 0,
				this.signatureValueSize);
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X.509");
			Collection<? extends Certificate> certificates = certificateFactory
					.generateCertificates(new ByteArrayInputStream(Arrays
							.copyOfRange(this.body, this.signatureValueSize,
									this.body.length)));
			this.certificateChain = new LinkedList<X509Certificate>();
			for (Certificate certificate : certificates) {
				this.certificateChain.add((X509Certificate) certificate);
			}
		} catch (CertificateException e) {
			throw new RuntimeException("cert parsing error: " + e.getMessage(),
					e);
		}
	}

	public byte[] signatureValue;

	public List<X509Certificate> certificateChain;
}
