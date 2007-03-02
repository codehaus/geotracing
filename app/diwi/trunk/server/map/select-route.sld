<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0"
					   xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
					   xmlns="http://www.opengis.net/sld"
					   xmlns:ogc="http://www.opengis.net/ogc"
					   xmlns:xlink="http://www.w3.org/1999/xlink"
					   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<NamedLayer>
		<Name>routes</Name>
		<UserStyle>
			<Title>Default Styler for routes</Title>
			<Abstract></Abstract>
			<FeatureTypeStyle>
				<FeatureTypeName>g_route</FeatureTypeName>
				<Rule>
					<ogc:Filter>
						<ogc:PropertyIsEqualTo>
							<ogc:PropertyName>id</ogc:PropertyName>
							<ogc:Literal>16</ogc:Literal>
						</ogc:PropertyIsEqualTo>
					</ogc:Filter>
					<LineSymbolizer>
						<Geometry>
							<PropertyName>center-line</PropertyName>
						</Geometry>
						<Stroke>
							<CssParameter name="stroke">#ff00cc</CssParameter>
							<CssParameter name="stroke-width">3</CssParameter>
							<!-- <CssParameter name="stroke-dasharray">1 1 1 1</CssParameter>  -->
						</Stroke>
					</LineSymbolizer>
				</Rule>
			</FeatureTypeStyle>
		</UserStyle>
	</NamedLayer>
</StyledLayerDescriptor>
