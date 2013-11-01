precision highp float;

uniform sampler2D u_Texture;

uniform float time;
uniform float offset;
uniform vec2 img_size;
uniform vec2 resolution;


void main(void)
{

	
	
	
	vec2 distanceVector = (gl_FragCoord.xy  / resolution) - 0.5;
	distanceVector.x -= (offset - 0.5) * 0.7;
	float len = length(vec2(distanceVector.x , distanceVector.y));
	float angle = atan(distanceVector.x, distanceVector.y) + time * 0.25 ;
	angle = mod(angle * 57.2957795,60.);
	float q = 1.0;
	if (angle < 30. ) {
	 q = 0.0; }
	vec4 color = vec4(mix(vec4(105.0/255.0, 210.0/255.0, 231.0/255.0, 1.0),vec4(243.0/255.0, 134.0/255.0, 48.0/255.0, 1.0),q  ) );
	color = mix(color * 0.5,color,gl_FragCoord.y / resolution.y);
	color = mix(color,color * 2.,1. - len);
	
	
	
	vec2 space = (vec2(resolution.x - img_size.x,resolution.y - img_size.y) * 0.5);
	vec2 point = ((gl_FragCoord.xy - space ) / img_size);
	point.x -= (offset - 0.5) * 0.5;
	point.y -= sin(time) * 0.05;
	if (0. < point.y && 0. < point.x    &&   1. > point.y && 1. > point.x) {
		
		color =  mix(texture2D(u_Texture, point),color,1. - texture2D(u_Texture, point).w);
	}
	gl_FragColor = color;
}