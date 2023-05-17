#version 300 es 
precision highp float;

out vec4 fragmentColor;
in vec4 rayDir;

uniform struct {
	samplerCube envTexture;
} material;

uniform struct {
  vec3 position;
  mat4 rayDirMatrix;
} camera;

uniform struct {
    vec4 position;
    vec3 powerDensity;
  } lights[8];

uniform struct {
  mat4 surface;
  mat4 clipper;
  //vec4 kd;
} quadrics[8];

vec3 shade(
  vec3 normal, vec3 lightDir,
  vec3 powerDensity, vec3 materialColor) {

  float cosa =
    clamp( dot(lightDir, normal),0.0,1.0);
  return
    powerDensity * materialColor * cosa;
}

float IntersectQuadric(vec4 e, vec4 d, mat4 A, mat4 B){
  float a = dot(d * A, d);
  float b = dot(d * A, e) + dot(e * A, d);
  float c = dot(e * A, e);

  float D = b * b - 4.0 * a * c;
  if(D<0.0){
    return -1.0;
  }
  D = sqrt(D);

  float t1 = (-b-D) / (2.0*a);
  float t2 = (-b+D) / (2.0*a);

  vec4 r1 = e + d * t1;
  if(dot(r1 * B, r1) >0.0){
    t1 = -1.0;
  }
  vec4 r2 = e + d * t2;
  if(dot(r2 * B, r2) >0.0){
    t2 = -1.0;
  }
  return (t1<0.0)?t2:((t2<0.0)?t1:min(t1, t2));
}

bool FindBestHit(vec4 e, vec4 d, out float bestT, out int bestI){
  bool hitFound = false;
  bestT = 10000.0;
  bestI = 0;
  for(int idx = 0; idx < 8; idx++){
    float newT = IntersectQuadric(e,d,quadrics[idx].surface, quadrics[idx].clipper);
    if(newT > 0.0){
      if(bestT > newT){
        bestT = newT;
        bestI = idx;
        hitFound = true;
      }
    }
  } 
  return hitFound;
}

void main(void) {
	vec4 e = vec4(camera.position, 1);
	vec4 d = vec4(normalize(rayDir.xyz), 0); 

  float bestT = -1.0;
  int bestI = 0;

  int killer = 10;
  while(FindBestHit(e, d, bestT, bestI) && killer > 0){
    mat4 A = quadrics[bestI].surface;
    float t = bestT;

    vec4 hit = e + d * t;
    vec3 normal = normalize((hit*A+A*hit).xyz);
    if(dot(d.xyz, normal) > 0.0){
      normal *= -1.0;
    }
    e = hit;
    e.xyz += normal * 0.001;

    d.xyz = reflect(d.xyz, normal);

    killer--;
    
    vec4 worldPos = A * hit;
    vec3 lightDiff = lights[0].position.xyz -
                   worldPos.xyz /* * lights[0].position.w*/;
    vec3 lightDir = normalize(lightDiff);

    float distanceSquared = dot(lightDiff, lightDiff);
    vec3 powerDensity =
     lights[0].powerDensity 
     / distanceSquared;

    fragmentColor.rgb += shade(normal, lightDir, powerDensity,
                             texture(material.envTexture, d.xyz).xyz);
  }
  
  fragmentColor = texture(material.envTexture, d.xyz);
}
