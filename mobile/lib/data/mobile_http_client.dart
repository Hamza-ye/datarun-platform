import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:http/io_client.dart';

const _debugTrustLabTlsDefine = 'DATARUN_DEBUG_TRUST_LAB_TLS';
const _debugTrustedTlsSha256Define = 'DATARUN_DEBUG_TRUSTED_TLS_SHA256';

http.Client createMobileHttpClient() {
  final pins = debugTrustedTlsPins();
  if (pins.isEmpty) return http.Client();

  final client = HttpClient(context: SecurityContext(withTrustedRoots: false));
  client.badCertificateCallback = (certificate, host, port) {
    return debugTrustedTlsPinMatches(
      pins: pins,
      host: host,
      certificateSha256: sha256.convert(certificate.der).toString(),
    );
  };
  return IOClient(client);
}

Map<String, String> debugTrustedTlsPins() {
  if (!kDebugMode) return const {};
  if (!const bool.fromEnvironment(_debugTrustLabTlsDefine)) return const {};

  return parseDebugTrustedTlsPins(
    const String.fromEnvironment(_debugTrustedTlsSha256Define),
  );
}

@visibleForTesting
Map<String, String> parseDebugTrustedTlsPins(String rawPins) {
  final pins = <String, String>{};
  for (final entry in rawPins.split(',')) {
    final separator = entry.indexOf('=');
    if (separator <= 0 || separator == entry.length - 1) continue;
    final host = normalizeDebugTrustedTlsHost(entry.substring(0, separator));
    final fingerprint = _normalizeFingerprint(entry.substring(separator + 1));
    if (host.isNotEmpty && fingerprint.length == 64) {
      pins[host] = fingerprint;
    }
  }
  return pins;
}

@visibleForTesting
bool debugTrustedTlsPinMatches({
  required Map<String, String> pins,
  required String host,
  required String certificateSha256,
}) {
  final expected = pins[normalizeDebugTrustedTlsHost(host)];
  if (expected == null) return false;
  return expected == _normalizeFingerprint(certificateSha256);
}

@visibleForTesting
String normalizeDebugTrustedTlsHost(String host) => host.trim().toLowerCase();

String _normalizeFingerprint(String value) =>
    value.replaceAll(RegExp(r'[^0-9a-fA-F]'), '').toLowerCase();
