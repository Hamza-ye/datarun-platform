import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:http/http.dart' as http;
import 'package:http/io_client.dart';

const _debugTrustLabTlsDefine = 'DATARUN_DEBUG_TRUST_LAB_TLS';
const _debugTrustedTlsSha256Define = 'DATARUN_DEBUG_TRUSTED_TLS_SHA256';

http.Client createMobileHttpClient() {
  final pins = debugTrustedTlsPins();
  if (pins.isEmpty) return http.Client();

  final client = HttpClient(context: SecurityContext(withTrustedRoots: false));
  client.badCertificateCallback = (certificate, host, port) {
    final expected = pins[host.toLowerCase()];
    if (expected == null) return false;
    final actual = sha256.convert(certificate.der).toString();
    return actual == expected;
  };
  return IOClient(client);
}

Map<String, String> debugTrustedTlsPins() {
  if (!_debugLabTlsTrustEnabled()) return const {};

  final pins = <String, String>{};
  for (final entry in _debugTrustedTlsSha256().split(',')) {
    final separator = entry.indexOf('=');
    if (separator <= 0 || separator == entry.length - 1) continue;
    final host = entry.substring(0, separator).trim().toLowerCase();
    final fingerprint = _normalizeFingerprint(entry.substring(separator + 1));
    if (host.isNotEmpty && fingerprint.length == 64) {
      pins[host] = fingerprint;
    }
  }
  return pins;
}

bool _debugLabTlsTrustEnabled() {
  var enabled = false;
  assert(() {
    enabled = const bool.fromEnvironment(_debugTrustLabTlsDefine);
    return true;
  }());
  return enabled;
}

String _debugTrustedTlsSha256() {
  var value = '';
  assert(() {
    value = const String.fromEnvironment(_debugTrustedTlsSha256Define);
    return true;
  }());
  return value;
}

String _normalizeFingerprint(String value) =>
    value.replaceAll(RegExp(r'[^0-9a-fA-F]'), '').toLowerCase();
